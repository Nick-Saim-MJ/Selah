package com.selahfinance.integraciones.infrastructure.security;

import com.selahfinance.integraciones.application.port.in.AutenticarClienteApiUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica a los sistemas de otros equipos por el header {@code X-API-Key}.
 * Cada scope del cliente se convierte en una authority {@code SCOPE_<scope>}
 * (p. ej. {@code @PreAuthorize("hasAuthority('SCOPE_habitos:write')")}).
 *
 * <p>No es un @Component a propósito: se registra solo en la cadena /api/v1/integraciones/**.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";

    private final AutenticarClienteApiUseCase autenticar;

    public ApiKeyAuthenticationFilter(AutenticarClienteApiUseCase autenticar) {
        this.autenticar = autenticar;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var cliente = autenticar.autenticar(request.getHeader(HEADER));
        if (cliente.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
            response.getWriter().write("""
                    {"title":"API key inválida","status":401,"detail":"Envía una API key válida en el header X-API-Key"}""");
            return;
        }
        var c = cliente.get();
        var authorities = c.scopes().stream().map(s -> new SimpleGrantedAuthority("SCOPE_" + s)).toList();
        var auth = UsernamePasswordAuthenticationToken.authenticated(c, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
