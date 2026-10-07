package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@org.springframework.security.test.context.support.WithMockUser(username = "admin")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private com.umc.pediupatrao.service.AuditoriaService auditoriaService;

    private Usuario usuario() {
        Usuario u = new Usuario();
        u.setId("1");
        u.setUsername("joao");
        u.setRole("USER");
        return u;
    }

    @Test
    void listarTodos_retorna200() throws Exception {
        when(usuarioService.listarTodos()).thenReturn(List.of(usuario()));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("joao"));
    }

    @Test
    void buscarPorId_existente_retorna200() throws Exception {
        when(usuarioService.buscarPorId("1")).thenReturn(Optional.of(usuario()));

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("joao"));
    }

    @Test
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(usuarioService.buscarPorId("404")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void criarUsuario_retorna201ECriptografa() throws Exception {
        mockMvc.perform(post("/api/usuarios").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"maria\",\"password\":\"segredo\",\"role\":\"USER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("maria"));

        verify(usuarioService).salvarUsuario(any(Usuario.class));
    }

    @Test
    void atualizarUsuario_existente_retorna200() throws Exception {
        when(usuarioService.buscarPorId("1")).thenReturn(Optional.of(usuario()));

        mockMvc.perform(put("/api/usuarios/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"joao.silva\",\"password\":\"nova\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("joao.silva"));

        verify(usuarioService).salvarUsuario(any(Usuario.class));
    }

    @Test
    void atualizarUsuario_inexistente_retorna404() throws Exception {
        when(usuarioService.buscarPorId("404")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/usuarios/404").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"x\",\"password\":\"y\"}"))
                .andExpect(status().isNotFound());

        verify(usuarioService, never()).salvarUsuario(any());
    }

    @Test
    void deletarUsuario_existente_retorna204() throws Exception {
        when(usuarioService.buscarPorId("1")).thenReturn(Optional.of(usuario()));

        mockMvc.perform(delete("/api/usuarios/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(usuarioService).deletarUsuario("1");
    }

    @Test
    void deletarUsuario_inexistente_retorna404() throws Exception {
        when(usuarioService.buscarPorId("404")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/usuarios/404").with(csrf()))
                .andExpect(status().isNotFound());

        verify(usuarioService, never()).deletarUsuario(anyString());
    }

    @Test
    void criarUsuario_semCsrf_retorna403() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"maria\",\"password\":\"segredo\"}"))
                .andExpect(status().isForbidden());
    }
}
