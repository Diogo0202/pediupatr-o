package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class PedidoService {

    /** Status iniciais e finais do fluxo de pedido. */
    public static final String PENDENTE = "PENDENTE";
    public static final String EM_PREPARO = "EM_PREPARO";
    public static final String EM_ENTREGA = "EM_ENTREGA";
    public static final String ENTREGUE = "ENTREGUE";
    public static final String CANCELADO = "CANCELADO";

    /**
     * Máquina de estados: de cada status quais transições são permitidas.
     * Nenhuma transição "para trás" é aceita (ex.: ENTREGUE volta a EM_PREPARO).
     */
    private static final Map<String, Set<String>> TRANSICOES = Map.of(
            PENDENTE, Set.of(EM_PREPARO, CANCELADO),
            EM_PREPARO, Set.of(EM_ENTREGA, CANCELADO),
            EM_ENTREGA, Set.of(ENTREGUE),
            ENTREGUE, Set.of(),
            CANCELADO, Set.of());

    private static final List<String> STATUS_VALIDOS = List.of(
            PENDENTE, EM_PREPARO, EM_ENTREGA, ENTREGUE, CANCELADO);

    /** Métodos de pagamento aceitos pelo sistema. */
    public static final List<String> METODOS_PAGAMENTO = List.of("DINHEIRO", "CARTAO", "PIX");

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    public Pedido criarPedido(Pedido pedido) {
        // Status inicial sempre PENDENTE quando não informado
        if (pedido.getStatus() == null || pedido.getStatus().isBlank()) {
            pedido.setStatus(PENDENTE);
        }
        validarStatusExistente(pedido.getStatus());

        // Data de criação (usada pelos gráficos do dashboard)
        if (pedido.getDataPedido() == null) {
            pedido.setDataPedido(Instant.now());
        }

        // Método de pagamento: padroniza e valida
        String metodo = pedido.getMetodoPagamento() == null || pedido.getMetodoPagamento().isBlank()
                ? "DINHEIRO" : pedido.getMetodoPagamento().trim().toUpperCase();
        if (!METODOS_PAGAMENTO.contains(metodo)) {
            throw new IllegalArgumentException(
                    "Método de pagamento inválido: " + metodo + ". Valores aceitos: " + METODOS_PAGAMENTO);
        }
        pedido.setMetodoPagamento(metodo);

        // Preço: usa o informado ou resolve pelo cadastro do produto (sabor)
        if (pedido.getValorUnitario() == null || pedido.getValorUnitario() < 0) {
            Optional<com.umc.pediupatrao.entity.Produto> produto =
                    produtoRepository.findByNomeIgnoreCase(pedido.getSabor() == null ? "" : pedido.getSabor());
            double preco = produto.map(com.umc.pediupatrao.entity.Produto::getPreco).orElse(0.0);
            pedido.setValorUnitario(preco);
        }

        // Total = unitário x quantidade
        double total = pedido.getValorUnitario() * pedido.getQuantidade();
        pedido.setValorTotal(Math.round(total * 100.0) / 100.0);

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(String id) {
        return pedidoRepository.findById(id);
    }

    public Pedido atualizarStatus(String id, String novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));

        if (novoStatus == null || novoStatus.isBlank()) {
            throw new IllegalArgumentException("Informe o novo status do pedido.");
        }

        validarStatusExistente(novoStatus);

        String atual = pedido.getStatus() == null ? PENDENTE : pedido.getStatus();
        if (atual.equals(novoStatus)) {
            throw new IllegalArgumentException("O pedido já está com status " + novoStatus + ".");
        }

        Set<String> permitidas = TRANSICOES.getOrDefault(atual, Set.of());
        if (!permitidas.contains(novoStatus)) {
            throw new IllegalArgumentException(
                    "Transição inválida: " + atual + " → " + novoStatus
                            + ". Próximos status permitidos: " + permitidas);
        }

        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    /** Exclui um pedido (usado apenas quando não há vínculo a manter). */
    public void excluir(String id) {
        pedidoRepository.deleteById(id);
    }

    private void validarStatusExistente(String status) {
        if (!STATUS_VALIDOS.contains(status)) {
            throw new IllegalArgumentException(
                    "Status inválido: " + status + ". Valores aceitos: " + STATUS_VALIDOS);
        }
    }

    // ===== Usado pelas telas (Thymeleaf) para renderizar as ações possíveis =====

    public static List<String> proximosStatus(String statusAtual) {
        if (statusAtual == null) {
            return List.of();
        }
        return List.copyOf(TRANSICOES.getOrDefault(statusAtual, Set.of()));
    }

    public static Collection<String> todosStatus() {
        return STATUS_VALIDOS;
    }
}
