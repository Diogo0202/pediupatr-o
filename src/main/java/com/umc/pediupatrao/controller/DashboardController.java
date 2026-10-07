package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoint consolidado do dashboard: KPIs (big numbers) e bases dos gráficos.
 * GET /api/dashboard/stats
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter ROTULO_DIA = DateTimeFormatter.ofPattern("dd/MM");

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        LocalDate hoje = LocalDate.now(ZONA);
        LocalDate mes = hoje.withDayOfMonth(1);

        List<Pedido> validos = pedidos.stream()
                .filter(p -> !PedidoService.CANCELADO.equals(p.getStatus()))
                .filter(p -> p.getDataPedido() != null)
                .toList();

        List<Pedido> doDia = validos.stream()
                .filter(p -> toDate(p).equals(hoje))
                .toList();

        List<Pedido> doMes = validos.stream()
                .filter(p -> !toDate(p).isBefore(mes))
                .toList();

        Map<String, Object> stats = new LinkedHashMap<>();

        // ===== Big numbers =====
        stats.put("faturamentoDia", soma(doDia));
        stats.put("faturamentoMes", soma(doMes));
        stats.put("pedidosDia", doDia.size());
        stats.put("pedidosMes", doMes.size());
        stats.put("pedidosTotal", pedidos.size());
        stats.put("ticketMedio", doMes.isEmpty() ? 0.0
                : arredonda(soma(doMes) / doMes.size()));
        stats.put("totalClientes", clienteRepository.count());
        stats.put("totalProdutos", produtoRepository.count());
        stats.put("pedidosAndamento", pedidos.stream()
                .filter(p -> !PedidoService.ENTREGUE.equals(p.getStatus())
                        && !PedidoService.CANCELADO.equals(p.getStatus()))
                .count());

        // ===== Gráfico de linha: últimos 7 dias =====
        List<Map<String, Object>> ultimos7 = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            List<Pedido> daData = validos.stream().filter(p -> toDate(p).equals(dia)).toList();
            Map<String, Object> ponto = new LinkedHashMap<>();
            ponto.put("data", dia.format(ROTULO_DIA));
            ponto.put("total", soma(daData));
            ponto.put("pedidos", daData.size());
            ultimos7.add(ponto);
        }
        stats.put("vendas7Dias", ultimos7);

        // ===== Gráfico de pizza: sabores mais vendidos =====
        Map<String, Integer> porSabor = validos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getSabor() == null ? "Não informado" : p.getSabor(),
                        Collectors.summingInt(Pedido::getQuantidade)));
        stats.put("sabores", porSabor.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(6)
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("sabor", e.getKey());
                    item.put("quantidade", e.getValue());
                    return item;
                })
                .toList());

        // ===== Gráfico de barras: métodos de pagamento =====
        stats.put("metodosPagamento", PedidoService.METODOS_PAGAMENTO.stream()
                .map(metodo -> {
                    List<Pedido> doMetodo = validos.stream()
                            .filter(p -> metodo.equals(p.getMetodoPagamento()))
                            .toList();
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("metodo", metodo);
                    item.put("total", soma(doMetodo));
                    item.put("pedidos", doMetodo.size());
                    return item;
                })
                .toList());

        // ===== Pedidos por status (badges do painel) =====
        stats.put("porStatus", PedidoService.todosStatus().stream()
                .map(status -> {
                    long qtd = pedidos.stream()
                            .filter(p -> status.equals(p.getStatus() == null
                                    ? PedidoService.PENDENTE : p.getStatus()))
                            .count();
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("status", status);
                    item.put("quantidade", qtd);
                    return item;
                })
                .toList());

        return stats;
    }

    private LocalDate toDate(Pedido p) {
        return p.getDataPedido().atZone(ZONA).toLocalDate();
    }

    private double soma(List<Pedido> pedidos) {
        double total = pedidos.stream()
                .mapToDouble(p -> p.getValorTotal() == null ? 0.0 : p.getValorTotal())
                .sum();
        return arredonda(total);
    }

    private double arredonda(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
