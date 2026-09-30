package com.selahfinance.identidad.application.usecase;

import com.selahfinance.identidad.application.dto.SesionResult;
import com.selahfinance.identidad.application.port.in.IniciarSesionUseCase;
import com.selahfinance.identidad.application.port.out.HogarPort;
import com.selahfinance.identidad.application.port.out.PasswordHasherPort;
import com.selahfinance.identidad.application.port.out.UsuarioRepositoryPort;
import com.selahfinance.shared.domain.DomainException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class IniciarSesionService implements IniciarSesionUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final PasswordHasherPort hasher;
    private final HogarPort hogares;
    private final SesionEmisor sesiones;

    @Override
    @Transactional(readOnly = true)
    public SesionResult iniciarSesion(String email, String password) {
        // Mismo mensaje para email inexistente y contraseña errónea: no revela qué cuentas existen.
        var credencialesInvalidas = new DomainException("CREDENCIALES_INVALIDAS", "Email o contraseña incorrectos");

        var usuario = usuarios.porEmail(email == null ? "" : email.trim().toLowerCase())
                .filter(u -> hasher.coincide(password, u.passwordHash()))
                .orElseThrow(() -> credencialesInvalidas);
        if (!usuario.puedeIniciarSesion()) {
            throw new DomainException("USUARIO_INACTIVO", "La cuenta está bloqueada. Contacta al administrador.");
        }
        UUID hogarId = null;
        if (usuario.rol().tieneFinanzas()) {
            hogarId = hogares.hogarPrincipalDe(usuario.id())
                    .orElseThrow(() -> new DomainException("SIN_HOGAR", "El usuario no pertenece a ningún hogar"));
        }
        return sesiones.emitir(usuario, hogarId);
    }
}
