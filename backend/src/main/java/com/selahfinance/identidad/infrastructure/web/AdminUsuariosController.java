package com.selahfinance.identidad.infrastructure.web;

import com.selahfinance.identidad.application.port.in.GestionarUsuariosUseCase;
import com.selahfinance.identidad.application.port.in.GestionarUsuariosUseCase.ComandoActualizar;
import com.selahfinance.identidad.application.port.in.GestionarUsuariosUseCase.ComandoCrear;
import com.selahfinance.identidad.application.port.in.GestionarUsuariosUseCase.Filtro;
import com.selahfinance.identidad.domain.model.EstadoUsuario;
import com.selahfinance.identidad.domain.model.RolUsuario;
import com.selahfinance.shared.infrastructure.web.PaginaResponse;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Solo ADMIN (regla en SecurityConfig + verificación del actor en el caso de uso). */
@Tag(name = "Admin · Usuarios")
@RestController
@RequestMapping("/api/v1/admin/usuarios")
@RequiredArgsConstructor
class AdminUsuariosController {

    private final GestionarUsuariosUseCase usuarios;

    @Operation(summary = "Lista cuentas con filtros (rol, iglesia, estado, texto)")
    @GetMapping
    PaginaResponse<UsuarioResponse> listar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) RolUsuario rol,
            @RequestParam(required = false) UUID iglesiaId,
            @RequestParam(required = false) EstadoUsuario estado,
            @RequestParam(name = "q", required = false) String texto,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {
        var resultado = usuarios.listar(actor(jwt), new Filtro(rol, iglesiaId, estado, texto, pagina, tamanio));
        return PaginaResponse.de(resultado, UsuarioResponse::de);
    }

    @Operation(summary = "Conteos de cuentas por rol y estado")
    @GetMapping("/resumen")
    ResumenResponse resumen(@AuthenticationPrincipal Jwt jwt) {
        var r = usuarios.resumen(actor(jwt));
        return new ResumenResponse(r.activosPorRol(), r.bloqueados(), r.total());
    }

    @Operation(summary = "Crea un pastor, hermano o admin con contraseña temporal")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UsuarioResponse crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CrearRequest r) {
        return UsuarioResponse.de(usuarios.crear(actor(jwt), new ComandoCrear(
                r.email(), r.nombres(), r.apellidos(), r.rol(), r.iglesiaId(), r.passwordTemporal(), r.moneda())));
    }

    @Operation(summary = "Edita datos, iglesia o rol (solo PASTOR ↔ HERMANO)")
    @PutMapping("/{id}")
    UsuarioResponse actualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            @Valid @RequestBody ActualizarRequest r) {
        return UsuarioResponse.de(usuarios.actualizar(actor(jwt), id,
                new ComandoActualizar(r.nombres(), r.apellidos(), r.rol(), r.iglesiaId())));
    }

    @Operation(summary = "Bloquea una cuenta (deja de funcionar de inmediato)")
    @PostMapping("/{id}/bloquear")
    UsuarioResponse bloquear(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return UsuarioResponse.de(usuarios.bloquear(actor(jwt), id));
    }

    @Operation(summary = "Reactiva una cuenta bloqueada")
    @PostMapping("/{id}/activar")
    UsuarioResponse activar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return UsuarioResponse.de(usuarios.activar(actor(jwt), id));
    }

    @Operation(summary = "Asigna una contraseña temporal (debe cambiarla al ingresar)")
    @PostMapping("/{id}/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void resetPassword(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody ResetRequest r) {
        usuarios.resetearPassword(actor(jwt), id, r.passwordTemporal());
    }

    private static UUID actor(Jwt jwt) {
        return UsuarioActual.de(jwt).usuarioId();
    }

    record CrearRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(max = 80) String nombres,
            @Size(max = 80) String apellidos,
            @NotNull RolUsuario rol,
            UUID iglesiaId,
            @NotBlank @Size(min = 8, max = 72) String passwordTemporal,
            @Pattern(regexp = "^[A-Z]{3}$", message = "Código ISO 4217, p. ej. PEN") String moneda) {
    }

    record ActualizarRequest(
            @Size(max = 80) String nombres,
            @Size(max = 80) String apellidos,
            RolUsuario rol,
            UUID iglesiaId) {
    }

    record ResetRequest(@NotBlank @Size(min = 8, max = 72) String passwordTemporal) {
    }

    record ResumenResponse(Map<RolUsuario, Long> activosPorRol, long bloqueados, long total) {
    }
}
