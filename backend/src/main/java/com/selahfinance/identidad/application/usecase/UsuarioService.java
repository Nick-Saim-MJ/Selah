package com.selahfinance.identidad.application.usecase;

import com.selahfinance.identidad.application.dto.UsuarioDetalle;
import com.selahfinance.identidad.application.port.in.ConsultarUsuarioUseCase;
import com.selahfinance.identidad.application.port.in.GestionarPerfilUseCase;
import com.selahfinance.identidad.application.port.in.GestionarUsuariosUseCase.Filtro;
import com.selahfinance.identidad.application.port.out.IglesiaPort;
import com.selahfinance.identidad.application.port.out.UsuarioRepositoryPort;
import com.selahfinance.identidad.domain.model.EstadoUsuario;
import com.selahfinance.identidad.domain.model.RolUsuario;
import com.selahfinance.identidad.domain.model.Usuario;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Perfil propio del usuario y consulta de cuentas para otros módulos. */
@Service
@RequiredArgsConstructor
class UsuarioService implements GestionarPerfilUseCase, ConsultarUsuarioUseCase {

    private static final int MAX_MIEMBROS = 5000;

    private final UsuarioRepositoryPort usuarios;
    private final IglesiaPort iglesias;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public UsuarioDetalle obtener(UUID usuarioId) {
        return detalle(cargar(usuarioId));
    }

    @Override
    @Transactional
    public UsuarioDetalle compartirReporte(UUID usuarioId, boolean compartir) {
        var actualizado = usuarios.guardar(cargar(usuarioId).compartirReporte(compartir, clock.instant()));
        return detalle(actualizado);
    }

    @Override
    @Transactional
    public UsuarioDetalle completarConfiguracionInicial(UUID usuarioId) {
        return detalle(usuarios.guardar(cargar(usuarioId).completarConfiguracionInicial()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> porId(UUID id) {
        return usuarios.porId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean sesionVigente(UUID usuarioId, RolUsuario rolDelToken) {
        return usuarios.porId(usuarioId)
                .map(u -> u.puedeIniciarSesion() && u.rol() == rolDelToken)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> hermanosActivosDe(UUID iglesiaId) {
        return usuarios.buscar(new Filtro(RolUsuario.HERMANO, iglesiaId, EstadoUsuario.ACTIVO, null, 0, MAX_MIEMBROS))
                .contenido();
    }

    private Usuario cargar(UUID id) {
        return usuarios.porId(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    private UsuarioDetalle detalle(Usuario u) {
        String nombre = u.iglesiaId() == null ? null : iglesias.nombres(List.of(u.iglesiaId())).get(u.iglesiaId());
        return new UsuarioDetalle(u, nombre);
    }
}
