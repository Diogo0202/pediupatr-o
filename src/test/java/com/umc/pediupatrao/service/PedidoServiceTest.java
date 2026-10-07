package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido novoPedido() {
        Pedido p = new Pedido();
        p.setClienteId("c1");
        p.setTipoPizza("Média");
        p.setSabor("Calabresa");
        p.setQuantidade(2);
        p.setValorUnitario(30.0);
        return p;
    }

    @Test
    void criarPedido_defineStatusDataMetodoETotal() {
        Pedido p = novoPedido();
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido salvo = pedidoService.criarPedido(p);

        assertEquals("PENDENTE", salvo.getStatus());
        assertEquals("DINHEIRO", salvo.getMetodoPagamento());
        assertNotNull(salvo.getDataPedido());
        assertEquals(60.0, salvo.getValorTotal(), 0.001);
        verify(pedidoRepository).save(p);
    }

    @Test
    void criarPedido_resolvePrecoPeloProdutoQuandoNaoInformado() {
        Pedido p = novoPedido();
        p.setValorUnitario(null);
        Produto produto = new Produto();
        produto.setNome("calabresa");
        produto.setPreco(42.5);
        when(produtoRepository.findByNomeIgnoreCase("Calabresa")).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido salvo = pedidoService.criarPedido(p);

        assertEquals(42.5, salvo.getValorUnitario(), 0.001);
        assertEquals(85.0, salvo.getValorTotal(), 0.001);
    }

    @Test
    void criarPedido_produtoNaoEncontrado_zeraValorUnitario() {
        Pedido p = novoPedido();
        p.setValorUnitario(null);
        when(produtoRepository.findByNomeIgnoreCase("Calabresa")).thenReturn(Optional.empty());
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido salvo = pedidoService.criarPedido(p);

        assertEquals(0.0, salvo.getValorUnitario(), 0.001);
        assertEquals(0.0, salvo.getValorTotal(), 0.001);
    }

    @Test
    void criarPedido_padronizaMetodoPagamentoParaMaiusculo() {
        Pedido p = novoPedido();
        p.setMetodoPagamento(" pix ");
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("PIX", pedidoService.criarPedido(p).getMetodoPagamento());
    }

    @Test
    void criarPedido_metodoInvalidoLancaExcecao() {
        Pedido p = novoPedido();
        p.setMetodoPagamento("CHEQUE");

        assertThrows(IllegalArgumentException.class, () -> pedidoService.criarPedido(p));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void criarPedido_statusInvalidoLancaExcecao() {
        Pedido p = novoPedido();
        p.setStatus("QUALQUER");

        assertThrows(IllegalArgumentException.class, () -> pedidoService.criarPedido(p));
    }

    @Test
    void criarPedido_arredondaValorTotalParaDuasCasas() {
        Pedido p = novoPedido();
        p.setQuantidade(3);
        p.setValorUnitario(10.333);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(31.0, pedidoService.criarPedido(p).getValorTotal(), 0.001);
    }

    @Test
    void atualizarStatus_transicaoValidaPersiste() {
        Pedido p = novoPedido();
        p.setStatus("PENDENTE");
        when(pedidoRepository.findById("1")).thenReturn(Optional.of(p));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido atualizado = pedidoService.atualizarStatus("1", "EM_PREPARO");

        assertEquals("EM_PREPARO", atualizado.getStatus());
    }

    @Test
    void atualizarStatus_transicaoInvalidaLancaExcecao() {
        Pedido p = novoPedido();
        p.setStatus("EM_ENTREGA");
        when(pedidoRepository.findById("1")).thenReturn(Optional.of(p));

        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.atualizarStatus("1", "PENDENTE"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void atualizarStatus_mesmoStatusLancaExcecao() {
        Pedido p = novoPedido();
        p.setStatus("PENDENTE");
        when(pedidoRepository.findById("1")).thenReturn(Optional.of(p));

        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.atualizarStatus("1", "PENDENTE"));
    }

    @Test
    void atualizarStatus_emBrancoLancaExcecao() {
        when(pedidoRepository.findById("1")).thenReturn(Optional.of(novoPedido()));

        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.atualizarStatus("1", "  "));
    }

    @Test
    void atualizarStatus_statusDesconhecidoLancaExcecao() {
        when(pedidoRepository.findById("1")).thenReturn(Optional.of(novoPedido()));

        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.atualizarStatus("1", "VOANDO"));
    }

    @Test
    void atualizarStatus_pedidoInexistenteLancaExcecao() {
        when(pedidoRepository.findById("404")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.atualizarStatus("404", "EM_PREPARO"));
    }

    @Test
    void atualizarStatus_pedidoCanceladoNaoTransiciona() {
        Pedido p = novoPedido();
        p.setStatus("CANCELADO");
        when(pedidoRepository.findById("1")).thenReturn(Optional.of(p));

        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.atualizarStatus("1", "EM_PREPARO"));
    }

    @Test
    void listarPedidos_devolveResultadoDoRepositorio() {
        List<Pedido> pedidos = List.of(novoPedido());
        when(pedidoRepository.findAll()).thenReturn(pedidos);

        assertEquals(pedidos, pedidoService.listarPedidos());
    }

    @Test
    void buscarPorId_devolveOptional() {
        Pedido p = novoPedido();
        when(pedidoRepository.findById("1")).thenReturn(Optional.of(p));
        when(pedidoRepository.findById("2")).thenReturn(Optional.empty());

        assertTrue(pedidoService.buscarPorId("1").isPresent());
        assertTrue(pedidoService.buscarPorId("2").isEmpty());
    }

    @Test
    void excluir_delegaParaRepositorio() {
        pedidoService.excluir("1");
        verify(pedidoRepository).deleteById("1");
    }

    @Test
    void proximosStatus_respeitaMaquinaDeEstados() {
        assertTrue(PedidoService.proximosStatus("PENDENTE").contains("EM_PREPARO"));
        assertTrue(PedidoService.proximosStatus("PENDENTE").contains("CANCELADO"));
        assertTrue(PedidoService.proximosStatus("ENTREGUE").isEmpty());
        assertTrue(PedidoService.proximosStatus("CANCELADO").isEmpty());
        assertTrue(PedidoService.proximosStatus(null).isEmpty());
    }

    @Test
    void todosStatus_listaOsCincoStatus() {
        assertEquals(5, PedidoService.todosStatus().size());
        assertTrue(PedidoService.todosStatus().contains("PENDENTE"));
        assertTrue(PedidoService.todosStatus().contains("CANCELADO"));
    }

    @Test
    void criarPedido_statusInicialPreservadoQuandoInformado() {
        Pedido p = novoPedido();
        p.setStatus("EM_PREPARO");
        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        pedidoService.criarPedido(p);

        verify(pedidoRepository).save(captor.capture());
        assertEquals("EM_PREPARO", captor.getValue().getStatus());
    }
}
