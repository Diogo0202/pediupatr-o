package com.umc.pediupatrao.config;

import com.umc.pediupatrao.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor de auditoria: registra toda requisição mutante (POST/PUT/PATCH/DELETE)
 * que chegue a um controller, incluindo usuário, IP e código de status.
 */
@Component
public class AuditoriaInterceptor implements HandlerInterceptor {

    private static final String ATRIBUTO_INICIO = "auditoriaInicio";

    private final AuditoriaService auditoriaService;

    public AuditoriaInterceptor(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATRIBUTO_INICIO, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (!(handler instanceof HandlerMethod)) {
            return;
        }

        String metodo = request.getMethod();
        if (!isMutante(metodo)) {
            return;
        }

        // Evita auto-registro do próprio endpoint de auditoria
        String uri = request.getRequestURI();
        if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/plugins")
                || uri.startsWith("/dist") || uri.startsWith("/images")) {
            return;
        }

        auditoriaService.registrar(usuarioAtual(), acao(metodo), uri,
                String.valueOf(response.getStatus()), ip(request), detalhe(handler));
    }

    private boolean isMutante(String metodo) {
        return "POST".equalsIgnoreCase(metodo) || "PUT".equalsIgnoreCase(metodo)
                || "PATCH".equalsIgnoreCase(metodo) || "DELETE".equalsIgnoreCase(metodo);
    }

    private String acao(String metodo) {
        return switch (metodo.toUpperCase()) {
            case "POST" -> "CRIACAO";
            case "PUT", "PATCH" -> "ALTERACAO";
            case "DELETE" -> "EXCLUSAO";
            default -> metodo;
        };
    }

    private String usuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "-";
        }
        return auth.getName();
    }

    private String ip(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String detalhe(Object handler) {
        if (handler instanceof HandlerMethod method) {
            return method.getBeanType().getSimpleName() + "#" + method.getMethod().getName();
        }
        return "-";
    }
}
