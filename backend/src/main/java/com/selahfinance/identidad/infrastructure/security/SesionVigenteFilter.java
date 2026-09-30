package com.selahfinance.identidad.infrastructure.security;

import com.selahfinance.identidad.application.port.in.ConsultarUsuarioUseCase;
import com.selahfinance.identidad.domain.model.RolUsuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Un JWT válido no basta: si el admin bloqueó la cuenta o cambió su rol, el token deja de servir
 * en la siguiente petición (no espera a que expire). Cuesta una consulta por PK por petición.
 *
 * <p>No es un @Component a propósito: se registra solo en la cadena de la app móvil (SecurityConfig).
 */
public class SesionVigenteFilter extends OncePerRequestFilter {

    private final ConsultarUsuarioUseCase usuarios;

    public SesionVigenteFilter(ConsultarUsuarioUseCase usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken auth
                && !vigente(auth)) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
            response.getWriter().write("""
                    {"title":"Sesión no vigente","status":401,"detail":"Tu cuenta fue bloqueada o modificada. Inicia sesión nuevamente."}""");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean vigente(JwtAuthenticationToken auth) {
        try {
            var jwt = auth.getToken();
            return usuarios.sesionVigente(UUID.fromString(jwt.getSubject()), RolUsuario.valueOf(jwt.getClaimAsString("rol")));
        } catch (IllegalArgumentException | NullPointerException e) {
            return false;
        }
    }
}
