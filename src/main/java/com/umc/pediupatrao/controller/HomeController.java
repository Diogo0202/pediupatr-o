package com.umc.pediupatrao.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.service.AuditoriaService;
import com.umc.pediupatrao.service.ClienteService;
import com.umc.pediupatrao.service.PedidoService;
import com.umc.pediupatrao.service.ProdutoService;
import com.umc.pediupatrao.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String realizarCadastro(@RequestParam(value = "email", defaultValue = "") String email,
                                   @RequestParam(value = "password", defaultValue = "") String password,
                                   @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                   @RequestParam(value = "role", defaultValue = "ATENDENTE") String role,
                                   @RequestParam(value = "consentimentoLgpd", defaultValue = "false") boolean consentimentoLgpd,
                                   RedirectAttributes redirectAttributes) {
        String emailTrimmed = email != null ? email.trim().toLowerCase() : "";
        if (emailTrimmed.isBlank() || !emailTrimmed.contains("@")) {
            redirectAttributes.addFlashAttribute("erro", "Por favor, informe um endereço de e-mail válido.");
            return "redirect:/cadastro";
        }

        if (password == null || password.trim().length() < 6) {
            redirectAttributes.addFlashAttribute("erro", "A senha deve conter no mínimo 6 caracteres.");
            redirectAttributes.addFlashAttribute("emailSalvo", emailTrimmed);
            redirectAttributes.addFlashAttribute("roleSalva", role);
            return "redirect:/cadastro";
        }

        if (confirmPassword != null && !password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("erro", "As senhas informadas não coincidem.");
            redirectAttributes.addFlashAttribute("emailSalvo", emailTrimmed);
            redirectAttributes.addFlashAttribute("roleSalva", role);
            return "redirect:/cadastro";
        }

        if (!consentimentoLgpd) {
            redirectAttributes.addFlashAttribute("erro", "É obrigatório aceitar os Termos de Uso e a Política de Privacidade (LGPD).");
            redirectAttributes.addFlashAttribute("emailSalvo", emailTrimmed);
            redirectAttributes.addFlashAttribute("roleSalva", role);
            return "redirect:/cadastro";
        }

        String roleNormalizada = role != null ? role.trim().toUpperCase() : "ATENDENTE";
        if (!List.of("ATENDENTE", "GERENTE", "FORNECEDOR").contains(roleNormalizada)) {
            roleNormalizada = "ATENDENTE";
        }

        if (usuarioService.existeUsernameOuEmail(emailTrimmed, emailTrimmed)) {
            redirectAttributes.addFlashAttribute("erro", "Já existe uma conta cadastrada com este e-mail.");
            redirectAttributes.addFlashAttribute("emailSalvo", emailTrimmed);
            redirectAttributes.addFlashAttribute("roleSalva", role);
            return "redirect:/cadastro";
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setUsername(emailTrimmed);
        novoUsuario.setEmail(emailTrimmed);
        novoUsuario.setPassword(password);
        novoUsuario.setRole(roleNormalizada);

        usuarioService.salvarUsuario(novoUsuario);

        auditoriaService.registrar(emailTrimmed, "CRIACAO", "/cadastro", "201", "127.0.0.1",
                "Novo usuário registrado no perfil: " + roleNormalizada);

        redirectAttributes.addFlashAttribute("sucesso",
                "Cadastro concluído com sucesso! Entre com seu e-mail e senha para acessar.");
        return "redirect:/login";
    }

    @GetMapping({"/termos-de-uso", "/termos"})
    public String termosDeUso(Model model) {
        model.addAttribute("content", "termos-de-uso :: content");
        return "termos-de-uso";
    }

    @GetMapping({"/politica-privacidade", "/politica-de-privacidade", "/politica-seguranca"})
    public String politicaPrivacidade(Model model) {
        model.addAttribute("content", "politica-privacidade :: content");
        return "politica-privacidade";
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("content", "home :: content");
        return "layout";
    }

    // ========================
    // USUÁRIOS
    // ========================
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = (List<Usuario>) usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("content", "usuarios/lista :: content");
        log.info("Carregando fragmento: usuarios/lista :: content");
        return "layout";
    }

    @GetMapping("/usuarios/novo")
    public String novoUsuarioForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("content", "usuarios/form :: content");
        return "layout";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuarioForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = usuarioService.buscarPorId(id).map(u -> {
            // Nunca expõe o hash da senha no formulário (o bind devolveria o valor ao salvar)
            u.setPassword(null);
            model.addAttribute("usuario", u);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
            return "redirect:/usuarios";
        }

        model.addAttribute("content", "usuarios/form :: content");
        return "layout";
    }

    @PostMapping("/usuarios/salvar")
    public String salvarUsuario(@Valid @ModelAttribute Usuario usuario,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("erro", mensagens(result));
            return "redirect:/usuarios/novo";
        }
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            redirectAttributes.addFlashAttribute("erro", "password: informe a senha do usuário.");
            return "redirect:/usuarios/novo";
        }
        usuarioService.salvarUsuario(usuario);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário criado com sucesso!");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/editar/{id}")
    public String atualizarUsuario(@PathVariable String id,
            @Valid @ModelAttribute Usuario usuario,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("erro", mensagens(result));
            return "redirect:/usuarios/editar/" + id;
        }
        usuarioService.atualizarUsuario(id, usuario);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário atualizado com sucesso!");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/deletar/{id}")
    public String deletarUsuario(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        usuarioService.deletarUsuario(id);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário removido com sucesso!");
        return "redirect:/usuarios";
    }

    // ========================
    // PEDIDOS
    // ========================
    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        List<Pedido> pedidos = pedidoService.listarPedidos();
        Map<String, String> nomesClientes = clienteService.listarClientes().stream()
                .collect(Collectors.toMap(Cliente::getId, Cliente::getNome, (a, b) -> a));
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("nomesClientes", nomesClientes);
        model.addAttribute("content", "pedidos :: content");
        log.info("Carregando fragmento: pedidos :: content");
        return "layout";
    }

    @GetMapping("/pedidos/novo")
    public String novoPedidoForm(Model model, RedirectAttributes redirectAttributes) {
        List<Cliente> clientes = clienteService.listarClientes();
        if (clientes.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro",
                    "Cadastre ao menos um cliente antes de criar um pedido.");
            return "redirect:/clientes/novo";
        }
        model.addAttribute("pedido", new Pedido());
        model.addAttribute("clientes", clientes);
        model.addAttribute("produtos", produtoService.listarProdutos());
        model.addAttribute("metodos", PedidoService.METODOS_PAGAMENTO);
        model.addAttribute("tipos", List.of("Broto", "Média", "Grande", "Família"));
        model.addAttribute("content", "pedidos/form :: content");
        return "layout";
    }

    @PostMapping("/pedidos/salvar")
    public String salvarPedido(@Valid @ModelAttribute Pedido pedido,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("erro", mensagens(result));
            return "redirect:/pedidos/novo";
        }
        try {
            pedidoService.criarPedido(pedido);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/pedidos/novo";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Pedido criado com sucesso!");
        return "redirect:/pedidos";
    }

    // ========================
    // PRODUTOS
    // ========================
    @GetMapping("/produtos")
    public String produtos(Model model) {
        List<Produto> produtos = produtoService.listarProdutos();
        model.addAttribute("produtos", produtos);
        model.addAttribute("content", "produtos/lista :: content");
        log.info("Carregando fragmento: produtos/lista :: content");
        return "layout";
    }

    // ========================
    // CLIENTES
    // ========================
    @GetMapping("/clientes")
    public String clientes(Model model) {
        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        model.addAttribute("content", "clientes/lista :: content");
        log.info("Carregando fragmento: clientes/lista :: content");
        return "layout";
    }

    @GetMapping("/clientes/novo")
    public String novoClienteForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("content", "clientes/form :: content");
        return "layout";
    }

    @PostMapping("/clientes/salvar")
    public String salvarCliente(@Valid @ModelAttribute Cliente cliente,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("erro", mensagens(result));
            return "redirect:/clientes/novo";
        }
        clienteService.salvar(cliente);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente criado com sucesso!");
        return "redirect:/clientes";
    }

    @GetMapping("/clientes/editar/{id}")
    public String editarClienteForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = clienteService.buscarPorId(id).map(c -> {
            model.addAttribute("cliente", c);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Cliente não encontrado.");
            return "redirect:/clientes";
        }

        model.addAttribute("content", "clientes/form :: content");
        return "layout";
    }

    @PostMapping("/clientes/editar/{id}")
    public String atualizarCliente(@PathVariable String id,
            @Valid @ModelAttribute Cliente cliente,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("erro", mensagens(result));
            return "redirect:/clientes/editar/" + id;
        }
        cliente.setId(id);
        clienteService.salvar(cliente);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente atualizado com sucesso!");
        return "redirect:/clientes";
    }

    /**
     * LGPD - Art. 18, VI: eliminação dos dados pessoais.
     * Remove o cliente apenas se ele não possuir pedidos vinculados;
     * caso contrário, anonimiza os dados preservando o histórico.
     */
    @PostMapping("/clientes/lgpd/excluir/{id}")
    public String excluirDadosPessoais(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        if (clienteService.temPedidosVinculados(id)) {
            clienteService.anonimizarDados(id);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Dados pessoais anonimizados (histórico de pedidos preservado).");
        } else {
            clienteService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Cliente excluído e dados pessoais removidos (LGPD).");
        }
        return "redirect:/clientes";
    }

    // ========================
    // OUTRAS ROTAS
    // ========================
    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {
        model.addAttribute("content", "configuracoes :: content");
        return "layout";
    }

    @GetMapping("/teste")
    public String teste() {
        return "teste";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    // ========================
    // AUDITORIA / SEGURANÇA
    // ========================
    @GetMapping("/admin/auditoria")
    public String auditoria(Model model) {
        model.addAttribute("logs", auditoriaService.listarRecentes());
        model.addAttribute("content", "admin/auditoria :: content");
        return "layout";
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado(Model model) {
        model.addAttribute("content", "acesso-negado :: content");
        return "layout";
    }

    /** Une as mensagens de validação em um único texto para o flash attribute. */
    private String mensagens(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(" | "));
    }
}
