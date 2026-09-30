package com.selahfinance.identidad.application.usecase;

import com.selahfinance.identidad.application.port.in.CrearAdminInicialUseCase;
import com.selahfinance.identidad.application.port.out.PasswordHasherPort;
import com.selahfinance.identidad.application.port.out.UsuarioRepositoryPort;
import com.selahfinance.identidad.domain.model.RolUsuario;
import com.selahfinance.identidad.domain.model.Usuario;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class AdminInicialService implements CrearAdminInicialUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final PasswordHasherPort hasher;

    @Override
    @Transactional
    public Optional<Usuario> crearSiNoExiste(String email, String password, String nombres, boolean forzarCambio) {
        Usuario.validarPassword(password);
        if (usuarios.existeEmail(email.trim().toLowerCase())) {
            return Optional.empty();
        }
        var admin = Usuario.crearPorAdmin(email, hasher.hashear(password), nombres, null, null, RolUsuario.ADMIN)
                .conPassword(hasher.hashear(password), forzarCambio);
        return Optional.of(usuarios.guardar(admin));
    }
}
