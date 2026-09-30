package com.selahfinance.identidad.application.usecase;

import com.selahfinance.identidad.application.port.in.CambiarPasswordUseCase;
import com.selahfinance.identidad.application.port.out.PasswordHasherPort;
import com.selahfinance.identidad.application.port.out.UsuarioRepositoryPort;
import com.selahfinance.identidad.domain.model.Usuario;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import com.selahfinance.shared.domain.DomainException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CambiarPasswordService implements CambiarPasswordUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final PasswordHasherPort hasher;

    @Override
    @Transactional
    public void cambiar(UUID usuarioId, String passwordActual, String passwordNueva) {
        var usuario = usuarios.porId(usuarioId).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));
        if (!hasher.coincide(passwordActual, usuario.passwordHash())) {
            throw new DomainException("PASSWORD_ACTUAL_INCORRECTA", "La contraseña actual no es correcta");
        }
        Usuario.validarPassword(passwordNueva);
        if (passwordNueva.equals(passwordActual)) {
            throw new DomainException("PASSWORD_IGUAL", "La contraseña nueva debe ser distinta de la actual");
        }
        usuarios.guardar(usuario.conPassword(hasher.hashear(passwordNueva), false));
    }
}
