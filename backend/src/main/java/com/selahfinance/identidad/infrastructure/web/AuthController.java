package com.selahfinance.identidad.infrastructure.web;

import com.selahfinance.identidad.application.dto.SesionResult;
import com.selahfinance.identidad.application.port.in.CambiarPasswordUseCase;
import com.selahfinance.identidad.application.port.in.IniciarSesionUseCase;
import com.selahfinance.identidad.application.port.in.RegistrarUsuarioUseCase;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticación")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
class AuthController {

    private final RegistrarUsuarioUseCase registrarUsuario;
    private final IniciarSesionUseCase iniciarSesion;
    private final CambiarPasswordUseCase cambiarPassword;

    @Operation(summary = "Auto-registro de un hermano en la iglesia elegida; devuelve la sesión")
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    SesionResult registro(@Valid @RequestBody RegistroRequest r) {
        return registrarUsuario.registrar(new RegistrarUsuarioUseCase.Comando(
                r.email(), r.password(), r.nombres(), r.apellidos(), r.iglesiaId(), r.nombreHogar(), r.moneda()));
    }

    @Operation(summary = "Inicia sesión con email y contraseña")
    @PostMapping("/login")
    SesionResult login(@Valid @RequestBody LoginRequest r) {
        return iniciarSesion.iniciarSesion(r.email(), r.password());
    }

    @Operation(summary = "Cambia la contraseña propia (obligatorio tras recibir una temporal)")
    @PostMapping("/cambiar-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cambiarPassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CambiarPasswordRequest r) {
        cambiarPassword.cambiar(UsuarioActual.de(jwt).usuarioId(), r.passwordActual(), r.passwordNueva());
    }

    record RegistroRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 80) String nombres,
            @Size(max = 80) String apellidos,
            @NotNull UUID iglesiaId,
            @Size(max = 100) String nombreHogar,
            @Pattern(regexp = "^[A-Z]{3}$", message = "Código ISO 4217, p. ej. PEN") String moneda) {
    }

    record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    record CambiarPasswordRequest(
            @NotBlank String passwordActual,
            @NotBlank @Size(min = 8, max = 72) String passwordNueva) {
    }
}
