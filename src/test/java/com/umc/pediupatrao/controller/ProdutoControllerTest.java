package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.service.ProdutoService;
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

@WebMvcTest(ProdutoController.class)
@WithMockUser(username = "admin")
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoService produtoService;

    @MockitoBean
    private com.umc.pediupatrao.service.AuditoriaService auditoriaService;

    private static final String JSON_VALIDO =
            "{\"nome\":\"Calabresa\",\"tipo\":\"Pizza\",\"preco\":45.0,\"ativo\":true}";

    private Produto produto() {
        Produto p = new Produto();
        p.setId("1");
        p.setNome("Calabresa");
        p.setTipo("Pizza");
        p.setPreco(45.0);
        p.setAtivo(true);
        return p;
    }

    @Test
    void listarProdutos_retorna200() throws Exception {
        when(produtoService.listarProdutos()).thenReturn(List.of(produto()));

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Calabresa"));
    }

    @Test
    void salvarProduto_valido_retorna201() throws Exception {
        when(produtoService.salvar(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/produtos").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Calabresa"));
    }

    @Test
    void salvarProduto_invalido_retorna400ComCampos() throws Exception {
        mockMvc.perform(post("/api/produtos").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"tipo\":\"\",\"preco\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.preco").exists());

        verify(produtoService, never()).salvar(any());
    }

    @Test
    void atualizarProduto_valido_retorna200() throws Exception {
        when(produtoService.salvar(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/produtos/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void excluirProduto_retorna204() throws Exception {
        mockMvc.perform(delete("/api/produtos/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(produtoService).excluir("1");
    }

    @Test
    void salvarProduto_regraDeNegocio_retorna400() throws Exception {
        when(produtoService.salvar(any(Produto.class)))
                .thenThrow(new IllegalArgumentException("Produto não encontrado"));

        mockMvc.perform(post("/api/produtos").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Produto não encontrado"));
    }
}
