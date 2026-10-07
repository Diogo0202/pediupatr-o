package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.service.AuditoriaService;
import com.umc.pediupatrao.service.ClienteService;
import com.umc.pediupatrao.service.PedidoService;
import com.umc.pediupatrao.service.ProdutoService;
import com.umc.pediupatrao.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
@WithMockUser(username = "admin")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private ProdutoService produtoService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private AuditoriaService auditoriaService;

    private Cliente cliente() {
        Cliente c = new Cliente();
        c.setId("c1");
        c.setNome("João");
        c.setConsentimentoLgpd(true);
        return c;
    }

    private Pedido pedidoComCliente() {
        Pedido p = new Pedido();
        p.setClienteId("c1");
        p.setSabor("Calabresa");
        p.setQuantidade(1);
        return p;
    }

    @Test
    void home_retorna200() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void login_retorna200() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void cadastro_retorna200() throws Exception {
        mockMvc.perform(get("/cadastro")).andExpect(status().isOk());
    }

    @Test
    void realizarCadastro_comSucessoRedirecionaParaLogin() throws Exception {
        when(usuarioService.existeUsernameOuEmail("novo@pizzaria.com", "novo@pizzaria.com")).thenReturn(false);

        mockMvc.perform(post("/cadastro")
                        .with(csrf())
                        .param("email", "novo@pizzaria.com")
                        .param("password", "senha123")
                        .param("confirmPassword", "senha123")
                        .param("role", "ATENDENTE")
                        .param("consentimentoLgpd", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(usuarioService).salvarUsuario(any(Usuario.class));
    }

    @Test
    void realizarCadastro_emailDuplicadoRedirecionaParaCadastroComErro() throws Exception {
        when(usuarioService.existeUsernameOuEmail("existente@pizzaria.com", "existente@pizzaria.com")).thenReturn(true);

        mockMvc.perform(post("/cadastro")
                        .with(csrf())
                        .param("email", "existente@pizzaria.com")
                        .param("password", "senha123")
                        .param("confirmPassword", "senha123")
                        .param("role", "GERENTE")
                        .param("consentimentoLgpd", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cadastro"));

        verify(usuarioService, never()).salvarUsuario(any());
    }

    @Test
    void realizarCadastro_senhasDivergentesRedirecionaParaCadastroComErro() throws Exception {
        mockMvc.perform(post("/cadastro")
                        .with(csrf())
                        .param("email", "teste@pizzaria.com")
                        .param("password", "senha123")
                        .param("confirmPassword", "outraSenha")
                        .param("role", "FORNECEDOR")
                        .param("consentimentoLgpd", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cadastro"));

        verify(usuarioService, never()).salvarUsuario(any());
    }

    @Test
    void realizarCadastro_semLgpdRedirecionaParaCadastroComErro() throws Exception {
        mockMvc.perform(post("/cadastro")
                        .with(csrf())
                        .param("email", "teste@pizzaria.com")
                        .param("password", "senha123")
                        .param("confirmPassword", "senha123")
                        .param("role", "ATENDENTE")
                        .param("consentimentoLgpd", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cadastro"));

        verify(usuarioService, never()).salvarUsuario(any());
    }

    @Test
    void termosDeUso_retorna200() throws Exception {
        mockMvc.perform(get("/termos-de-uso")).andExpect(status().isOk());
        mockMvc.perform(get("/termos")).andExpect(status().isOk());
    }

    @Test
    void politicaPrivacidade_retorna200() throws Exception {
        mockMvc.perform(get("/politica-privacidade")).andExpect(status().isOk());
        mockMvc.perform(get("/politica-de-privacidade")).andExpect(status().isOk());
        mockMvc.perform(get("/politica-seguranca")).andExpect(status().isOk());
    }

    @Test
    void acessoNegado_retorna200() throws Exception {
        mockMvc.perform(get("/acesso-negado")).andExpect(status().isOk());
    }

    @Test
    void listarPedidos_retorna200ComNomesDosClientes() throws Exception {
        when(pedidoService.listarPedidos()).thenReturn(List.of(pedidoComCliente()));
        when(clienteService.listarClientes()).thenReturn(List.of(cliente()));

        mockMvc.perform(get("/pedidos")).andExpect(status().isOk());
    }

    @Test
    void novoPedidoForm_comClientes_retorna200() throws Exception {
        when(clienteService.listarClientes()).thenReturn(List.of(cliente()));
        when(produtoService.listarProdutos()).thenReturn(List.of(new Produto()));

        mockMvc.perform(get("/pedidos/novo")).andExpect(status().isOk());
    }

    @Test
    void novoPedidoForm_semClientes_redirecionaParaCadastro() throws Exception {
        when(clienteService.listarClientes()).thenReturn(List.of());

        mockMvc.perform(get("/pedidos/novo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clientes/novo"));
    }

    @Test
    void salvarPedido_valido_redirecionaParaListaComSucesso() throws Exception {
        when(pedidoService.criarPedido(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/pedidos/salvar").with(csrf())
                        .param("clienteId", "c1")
                        .param("tipoPizza", "Média")
                        .param("sabor", "Calabresa")
                        .param("quantidade", "2")
                        .param("metodoPagamento", "PIX"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pedidos"));

        verify(pedidoService).criarPedido(any(Pedido.class));
    }

    @Test
    void salvarPedido_invalido_redirecionaParaFormulario() throws Exception {
        mockMvc.perform(post("/pedidos/salvar").with(csrf())
                        .param("tipoPizza", "Média")
                        .param("sabor", "Calabresa")
                        .param("quantidade", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pedidos/novo"));

        verify(pedidoService, never()).criarPedido(any());
    }

    @Test
    void salvarPedido_regraDeNegocio_redirecionaParaFormulario() throws Exception {
        when(pedidoService.criarPedido(any(Pedido.class)))
                .thenThrow(new IllegalArgumentException("Método de pagamento inválido"));

        mockMvc.perform(post("/pedidos/salvar").with(csrf())
                        .param("clienteId", "c1")
                        .param("tipoPizza", "Média")
                        .param("sabor", "Calabresa")
                        .param("quantidade", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pedidos/novo"));
    }

    @Test
    void paginaClientes_retorna200() throws Exception {
        when(clienteService.listarClientes()).thenReturn(List.of(cliente()));

        mockMvc.perform(get("/clientes")).andExpect(status().isOk());
    }

    @Test
    void paginaProdutos_retorna200() throws Exception {
        when(produtoService.listarProdutos()).thenReturn(List.of());

        mockMvc.perform(get("/produtos")).andExpect(status().isOk());
    }

    @Test
    void paginaUsuarios_retorna200() throws Exception {
        Usuario u = new Usuario();
        u.setId("1");
        u.setUsername("joao");
        when(usuarioService.listarTodos()).thenReturn(List.of(u));

        mockMvc.perform(get("/usuarios")).andExpect(status().isOk());
    }

    @Test
    void editarUsuarioForm_existente_retorna200() throws Exception {
        Usuario u = new Usuario();
        u.setId("1");
        u.setUsername("joao");
        when(usuarioService.buscarPorId("1")).thenReturn(Optional.of(u));

        mockMvc.perform(get("/usuarios/editar/1")).andExpect(status().isOk());
    }

    @Test
    void editarUsuarioForm_inexistente_redireciona() throws Exception {
        when(usuarioService.buscarPorId("404")).thenReturn(Optional.empty());

        mockMvc.perform(get("/usuarios/editar/404"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void paginaAuditoria_retorna200() throws Exception {
        when(auditoriaService.listarRecentes()).thenReturn(List.of());

        mockMvc.perform(get("/admin/auditoria")).andExpect(status().isOk());
    }

    @Test
    void novoClienteForm_retorna200() throws Exception {
        mockMvc.perform(get("/clientes/novo")).andExpect(status().isOk());
    }

    @Test
    void configuracoes_retorna200() throws Exception {
        mockMvc.perform(get("/configuracoes")).andExpect(status().isOk());
    }
}
