package com.selahfinance.identidad.application.usecase;

import com.selahfinance.identidad.application.dto.SesionResult;
import com.selahfinance.identidad.application.port.in.RegistrarUsuarioUseCase;
import com.selahfinance.identidad.application.port.out.HogarPort;
import com.selahfinance.identidad.application.port.out.IglesiaPort;
import com.selahfinance.identidad.application.port.out.PasswordHasherPort;
import com.selahfinance.identidad.application.port.out.UsuarioRepositoryPort;
import com.selahfinance.identidad.domain.model.Usuario;
import com.selahfinance.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Auto-registro de un hermano en la iglesia que elige, con su hogar inicial y configuración por defecto. */
@Service
@RequiredArgsConstructor
class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final PasswordHasherPort hasher;
    private final HogarPort hogares;
    private final IglesiaPort iglesias;
    private final SesionEmisor sesiones;

    @Override
    @Transactional
    public SesionResult registrar(Comando c) {
        Usuario.validarPassword(c.password());
        String email = c.email() == null ? null : c.email().trim().toLowerCase();
        if (email != null && usuarios.existeEmail(email)) {
            throw new DomainException("EMAIL_REGISTRADO", "Ya existe una cuenta con ese email");
        }
        if (!iglesias.existeActiva(c.iglesiaId())) {
            throw new DomainException("IGLESIA_INVALIDA", "La iglesia elegida no existe o no está activa");
        }

        Usuario usuario = usuarios.guardar(
                Usuario.registrar(email, hasher.hashear(c.password()), c.nombres(), c.apellidos(), c.iglesiaId()));

        String nombreHogar = (c.nombreHogar() == null || c.nombreHogar().isBlank())
                ? "Hogar de " + usuario.nombres()
                : c.nombreHogar().trim();
        var hogarId = hogares.crearHogarInicial(usuario.id(), usuario.nombres(), nombreHogar, c.moneda());
        return sesiones.emitir(usuario, hogarId);
    }
}
