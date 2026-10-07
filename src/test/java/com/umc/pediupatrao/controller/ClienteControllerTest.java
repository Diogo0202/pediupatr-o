package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.service.AuditoriaService;
import com.umc.pediupatrao.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@WithMockUser(username = "admin")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private AuditoriaService auditoriaService;

    private static final String JSON_VALIDO =
            "{\"nome\":\"João\",\"telefone\":\"11999999999\",\"cep\":\"01001000\",\"consentimentoLgpd\":true}";

    private Cliente cliente() {
        Cliente c = new Cliente();
        c.setId("1");
        c.setNome("João");
        c.setConsentimentoLgpd(true);
        return c;
    }

    @Test
    void listarClientes_retorna200() throws Exception {
        when(clienteService.listarClientes()).thenReturn(List.of(cliente()));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("João"));
    }

    @Test
    void salvarCliente_valido_retorna201() throws Exception {
        when(clienteService.salvar(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/clientes").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("João"));
    }

    @Test
    void salvarCliente_invalido_retorna400ComCampos() throws Exception {
        mockMvc.perform(post("/api/clientes").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"consentimentoLgpd\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.consentimentoAceito").exists());

        verify(clienteService, never()).salvar(any());
    }

    @Test
    void atualizarCliente_valido_retorna200() throws Exception {
        when(clienteService.salvar(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/clientes/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void excluirCliente_retorna204() throws Exception {
        mockMvc.perform(delete("/api/clientes/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(clienteService).excluir("1");
    }

    @Test
    void excluirCliente_semCsrf_retorna403() throws Exception {
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isForbidden());
    }
}
