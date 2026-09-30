package com.selahfinance.identidad.infrastructure.web;

import com.selahfinance.identidad.application.port.in.GestionarPerfilUseCase;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Perfil")
@RestController
@RequestMapping("/api/v1/perfil")
@RequiredArgsConstructor
class PerfilController {

    private final GestionarPerfilUseCase perfil;

    @Operation(summary = "Mi cuenta: rol, iglesia y si comparto mi reporte con mi pastor")
    @GetMapping
    UsuarioResponse obtener(@AuthenticationPrincipal Jwt jwt) {
        return UsuarioResponse.de(perfil.obtener(UsuarioActual.de(jwt).usuarioId()));
    }

    @Operation(summary = "[HERMANO] Decide compartir (o dejar de compartir) mi reporte con mi pastor")
    @PutMapping("/compartir-reporte")
    UsuarioResponse compartirReporte(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CompartirRequest r) {
        return UsuarioResponse.de(perfil.compartirReporte(UsuarioActual.de(jwt).usuarioId(), r.compartir()));
    }

    @Operation(summary = "Marca terminado el asistente de configuración inicial")
    @PostMapping("/configuracion-inicial-completada")
    UsuarioResponse completarConfiguracion(@AuthenticationPrincipal Jwt jwt) {
        return UsuarioResponse.de(perfil.completarConfiguracionInicial(UsuarioActual.de(jwt).usuarioId()));
    }

    record CompartirRequest(boolean compartir) {
    }
}
