package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@WithMockUser(username = "admin")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoRepository pedidoRepository;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private ProdutoRepository produtoRepository;

    @MockitoBean
    private com.umc.pediupatrao.service.AuditoriaService auditoriaService;

    private Pedido pedido(String status, String metodo, Double total,
                          String sabor, int qtd, Instant data) {
        Pedido p = new Pedido();
        p.setClienteId("c1");
        p.setTipoPizza("Média");
        p.setSabor(sabor);
        p.setQuantidade(qtd);
        p.setStatus(status);
        p.setMetodoPagamento(metodo);
        p.setValorTotal(total);
        p.setDataPedido(data);
        return p;
    }

    @Test
    void stats_semDados_retornaZerosEVazio() throws Exception {
        when(pedidoRepository.findAll()).thenReturn(List.of());
        when(clienteRepository.count()).thenReturn(0L);
        when(produtoRepository.count()).thenReturn(0L);

        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faturamentoDia").value(0.0))
                .andExpect(jsonPath("$.faturamentoMes").value(0.0))
                .andExpect(jsonPath("$.pedidosDia").value(0))
                .andExpect(jsonPath("$.pedidosMes").value(0))
                .andExpect(jsonPath("$.pedidosTotal").value(0))
                .andExpect(jsonPath("$.ticketMedio").value(0.0))
                .andExpect(jsonPath("$.totalClientes").value(0))
                .andExpect(jsonPath("$.totalProdutos").value(0))
                .andExpect(jsonPath("$.pedidosAndamento").value(0))
                .andExpect(jsonPath("$.vendas7Dias", hasSize(7)))
                .andExpect(jsonPath("$.sabores", hasSize(0)))
                .andExpect(jsonPath("$.metodosPagamento", hasSize(3)))
                .andExpect(jsonPath("$.porStatus", hasSize(5)));
    }

    @Test
    void stats_comDados_consolidaKpisEGráficos() throws Exception {
        Instant agora = Instant.now();
        List<Pedido> pedidos = List.of(
                pedido("ENTREGUE", "PIX", 100.0, "Calabresa", 2, agora),
                pedido("EM_PREPARO", "CARTAO", 50.0, "Calabresa", 1, agora),
                pedido("CANCELADO", "DINHEIRO", 999.0, "Marguerita", 9, agora)
        );
        when(pedidoRepository.findAll()).thenReturn(pedidos);
        when(clienteRepository.count()).thenReturn(5L);
        when(produtoRepository.count()).thenReturn(4L);

        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faturamentoDia").value(150.0))
                .andExpect(jsonPath("$.faturamentoMes").value(150.0))
                .andExpect(jsonPath("$.pedidosDia").value(2))
                .andExpect(jsonPath("$.pedidosMes").value(2))
                .andExpect(jsonPath("$.pedidosTotal").value(3))
                .andExpect(jsonPath("$.ticketMedio").value(75.0))
                .andExpect(jsonPath("$.totalClientes").value(5))
                .andExpect(jsonPath("$.totalProdutos").value(4))
                .andExpect(jsonPath("$.pedidosAndamento").value(1))
                .andExpect(jsonPath("$.sabores[0].sabor").value("Calabresa"))
                .andExpect(jsonPath("$.sabores[0].quantidade").value(3))
                .andExpect(jsonPath("$.metodosPagamento[0].metodo").value("DINHEIRO"))
                .andExpect(jsonPath("$.metodosPagamento[0].total").value(0.0))
                .andExpect(jsonPath("$.metodosPagamento[1].metodo").value("CARTAO"))
                .andExpect(jsonPath("$.metodosPagamento[1].total").value(50.0))
                .andExpect(jsonPath("$.metodosPagamento[2].metodo").value("PIX"))
                .andExpect(jsonPath("$.metodosPagamento[2].total").value(100.0));
    }

    @Test
    void stats_pedidoCanceladoEntraApenasEmPedidosTotalEPorStatus() throws Exception {
        Instant agora = Instant.now();
        when(pedidoRepository.findAll()).thenReturn(List.of(
                pedido("CANCELADO", "DINHEIRO", 999.0, "Marguerita", 9, agora)));
        when(clienteRepository.count()).thenReturn(1L);
        when(produtoRepository.count()).thenReturn(1L);

        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faturamentoDia").value(0.0))
                .andExpect(jsonPath("$.pedidosDia").value(0))
                .andExpect(jsonPath("$.pedidosTotal").value(1))
                .andExpect(jsonPath("$.sabores", hasSize(0)))
                .andExpect(jsonPath("$.porStatus[4].status").value("CANCELADO"))
                .andExpect(jsonPath("$.porStatus[4].quantidade").value(1));
    }

    @Test
    void stats_pedidoDeOutroDia_naoContabilizaNoDia() throws Exception {
        Instant outroDia = Instant.now().minus(3, ChronoUnit.DAYS);
        when(pedidoRepository.findAll()).thenReturn(List.of(
                pedido("ENTREGUE", "PIX", 80.0, "Quatro Queijos", 1, outroDia)));
        when(clienteRepository.count()).thenReturn(1L);
        when(produtoRepository.count()).thenReturn(1L);

        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faturamentoDia").value(0.0))
                .andExpect(jsonPath("$.pedidosDia").value(0))
                .andExpect(jsonPath("$.pedidosTotal").value(1));
    }

    @Test
    void stats_pedidoSemData_naoQuebraEstatisticas() throws Exception {
        when(pedidoRepository.findAll()).thenReturn(List.of(
                pedido("PENDENTE", "DINHEIRO", null, "Calabresa", 1, null)));
        when(clienteRepository.count()).thenReturn(1L);
        when(produtoRepository.count()).thenReturn(1L);

        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faturamentoDia").value(0.0))
                .andExpect(jsonPath("$.pedidosTotal").value(1))
                .andExpect(jsonPath("$.pedidosAndamento").value(1));
    }
}
