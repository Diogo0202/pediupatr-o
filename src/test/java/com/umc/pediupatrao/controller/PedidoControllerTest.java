package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@WithMockUser(username = "admin")
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private com.umc.pediupatrao.service.AuditoriaService auditoriaService;

    private static final String JSON_VALIDO =
            "{\"clienteId\":\"c1\",\"tipoPizza\":\"Média\",\"sabor\":\"Calabresa\",\"quantidade\":2}";

    private Pedido pedido() {
        Pedido p = new Pedido();
        p.setId("1");
        p.setClienteId("c1");
        p.setTipoPizza("Média");
        p.setSabor("Calabresa");
        p.setQuantidade(2);
        p.setValorTotal(60.0);
        p.setStatus("PENDENTE");
        return p;
    }

    @Test
    void criarPedido_dadosValidos_retorna201() throws Exception {
        when(pedidoService.criarPedido(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId("1");
            return p;
        });

        mockMvc.perform(post("/api/pedidos").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.clienteId").value("c1"))
                .andExpect(jsonPath("$.sabor").value("Calabresa"));
    }

    @Test
    void criarPedido_dadosInvalidos_retorna400ComCampos() throws Exception {
        mockMvc.perform(post("/api/pedidos").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoPizza\":\"Média\",\"sabor\":\"Calabresa\",\"quantidade\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.clienteId").exists())
                .andExpect(jsonPath("$.campos.quantidade").exists());

        verify(pedidoService, never()).criarPedido(any());
    }

    @Test
    void criarPedido_regraDeNegocio_retorna400ComErro() throws Exception {
        when(pedidoService.criarPedido(any(Pedido.class)))
                .thenThrow(new IllegalArgumentException("Método de pagamento inválido"));

        mockMvc.perform(post("/api/pedidos").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Método de pagamento inválido"));
    }

    @Test
    void criarPedido_semCsrf_retorna403() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarPedidos_retorna200ComLista() throws Exception {
        when(pedidoService.listarPedidos()).thenReturn(List.of(pedido()));

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sabor").value("Calabresa"));
    }

    @Test
    void buscarPorId_existente_retorna200() throws Exception {
        when(pedidoService.buscarPorId("1")).thenReturn(Optional.of(pedido()));

        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(pedidoService.buscarPorId("404")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pedidos/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarStatus_valido_retorna200() throws Exception {
        Pedido p = pedido();
        p.setStatus("EM_PREPARO");
        when(pedidoService.atualizarStatus("1", "EM_PREPARO")).thenReturn(p);

        mockMvc.perform(put("/api/pedidos/1/status")
                        .param("status", "EM_PREPARO")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PREPARO"));
    }

    @Test
    void atualizarStatus_transicaoInvalida_retorna400() throws Exception {
        when(pedidoService.atualizarStatus(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Transição não permitida"));

        mockMvc.perform(put("/api/pedidos/1/status")
                        .param("status", "PENDENTE")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Transição não permitida"));
    }

    @Test
    void excluir_existente_retorna204() throws Exception {
        mockMvc.perform(delete("/api/pedidos/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(pedidoService).excluir("1");
    }

    @Test
    void proximosStatus_retornaTransicoesPermitidas() throws Exception {
        mockMvc.perform(get("/api/pedidos/status/permitidos").param("atual", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permitidos", hasItems("EM_PREPARO", "CANCELADO")));
    }

    @Test
    @WithAnonymousUser
    void semAutenticacao_retorna401NoSliceWebMvcTest() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isUnauthorized());
    }
}
