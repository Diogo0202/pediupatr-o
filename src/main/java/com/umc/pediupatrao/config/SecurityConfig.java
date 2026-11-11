package com.umc.pediupatrao.config;

import com.umc.pediupatrao.service.AuditoriaService;
import com.umc.pediupatrao.service.UsuarioDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;
    private final AuditoriaService auditoriaService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService, AuditoriaService auditoriaService) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.auditoriaService = auditoriaService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ CSRF ativo: formulários Thymeleaf recebem o token via _csrf;
                // o token também é exposto no cookie XSRF-TOKEN (legível por JS) para os fetch() do front
                .csrf(csrf -> csrf
                        .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository
                                .withHttpOnlyFalse()))
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos, login, cadastro e páginas de transparência LGPD permanecem públicos
                        .requestMatchers(
                                "/login",
                                "/cadastro",
                                "/cadastro/**",
                                "/termos-de-uso",
                                "/termos",
                                "/politica-privacidade",
                                "/politica-de-privacidade",
                                "/politica-seguranca",
                                "/css/**",
                                "/dist/**",
                                "/plugins/**",
                                "/js/**",
                                "/images/**",
                                "/error"
                        ).permitAll()
                        // Auditoria é exclusiva do administrador.
                        .requestMatchers("/admin/auditoria", "/admin/auditoria/**").hasRole("ADMIN")
                        // Gestão de usuários (REST e telas) restrita a ADMIN e GERENTE
                        .requestMatchers("/api/usuarios", "/api/usuarios/**", "/usuarios", "/usuarios/**")
                        .hasAnyRole("ADMIN", "GERENTE")
                        // Pedidos e clientes ficam disponíveis para atendimento e gestão.
                        .requestMatchers("/pedidos", "/pedidos/**", "/api/pedidos", "/api/pedidos/**")
                        .hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers("/clientes", "/clientes/**", "/api/clientes", "/api/clientes/**")
                        .hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        // Produtos e cardápio ficam disponíveis para fornecedores e gestão.
                        .requestMatchers("/produtos", "/produtos/**", "/api/produtos", "/api/produtos/**")
                        .hasAnyRole("ADMIN", "GERENTE", "FORNECEDOR")
                        .requestMatchers("/", "/configuracoes", "/api/dashboard/**")
                        .hasAnyRole("ADMIN", "GERENTE")
                        // Área administrativa restrita a ADMIN e GERENTE
                        .requestMatchers("/admin", "/admin/**").hasAnyRole("ADMIN", "GERENTE")
                        // Demais rotas exigem autenticação
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex.accessDeniedPage("/acesso-negado"));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            auditoriaService.registrar(authentication.getName(), "LOGIN",
                    request.getRequestURI(), "200", ip(request), "Login realizado com sucesso");

            boolean isAtendente = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ATENDENTE"));
            boolean isFornecedor = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_FORNECEDOR"));

            if (isAtendente) {
                response.sendRedirect("/pedidos");
            } else if (isFornecedor) {
                response.sendRedirect("/produtos");
            } else {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String username = request.getParameter("username");
            auditoriaService.registrar(username == null ? "-" : username, "LOGIN_FALHOU",
                    request.getRequestURI(), "401", ip(request),
                    "Tentativa de login inválida: " + exception.getMessage());
            response.sendRedirect("/login?error");
        };
    }

    private String ip(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
