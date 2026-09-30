package com.selahfinance.identidad.application.usecase;

import com.selahfinance.identidad.application.dto.UsuarioDetalle;
import com.selahfinance.identidad.application.port.in.GestionarUsuariosUseCase;
import com.selahfinance.identidad.application.port.out.HogarPort;
import com.selahfinance.identidad.application.port.out.IglesiaPort;
import com.selahfinance.identidad.application.port.out.PasswordHasherPort;
import com.selahfinance.identidad.application.port.out.UsuarioRepositoryPort;
import com.selahfinance.identidad.domain.model.EstadoUsuario;
import com.selahfinance.identidad.domain.model.RolUsuario;
import com.selahfinance.identidad.domain.model.Usuario;
import com.selahfinance.shared.application.Pagina;
import com.selahfinance.shared.application.exception.AccesoDenegadoException;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import com.selahfinance.shared.domain.DomainException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Panel del administrador. El actor se vuelve a cargar de la base de datos en cada operación
 * (no se confía en el rol del token) y no puede bloquearse ni cambiarse el rol a sí mismo.
 */
@Service
@RequiredArgsConstructor
class GestionUsuariosService implements GestionarUsuariosUseCase {

    private static final int TAMANIO_MAXIMO = 100;

    private final UsuarioRepositoryPort usuarios;
    private final IglesiaPort iglesias;
    private final PasswordHasherPort hasher;
    private final HogarPort hogares;

    @Override
    @Transactional(readOnly = true)
    public Pagina<UsuarioDetalle> listar(UUID actorId, Filtro f) {
        exigirAdmin(actorId);
        var pagina = usuarios.buscar(new Filtro(f.rol(), f.iglesiaId(), f.estado(), f.texto(),
                Math.max(f.pagina(), 0), Math.min(Math.max(f.tamanio(), 1), TAMANIO_MAXIMO)));
        var nombres = iglesias.nombres(pagina.contenido().stream().map(Usuario::iglesiaId).filter(Objects::nonNull)
                .distinct().toList());
        return pagina.map(u -> new UsuarioDetalle(u, u.iglesiaId() == null ? null : nombres.get(u.iglesiaId())));
    }

    @Override
    @Transactional
    public UsuarioDetalle crear(UUID actorId, ComandoCrear c) {
        exigirAdmin(actorId);
        Usuario.validarPassword(c.passwordTemporal());
        if (c.rol() == null) {
            throw new DomainException("ROL_REQUERIDO", "Indica el rol de la cuenta");
        }
        String email = c.email() == null ? null : c.email().trim().toLowerCase();
        if (email != null && usuarios.existeEmail(email)) {
            throw new DomainException("EMAIL_REGISTRADO", "Ya existe una cuenta con ese email");
        }
        if (c.rol() != RolUsuario.ADMIN) {
            exigirIglesiaActiva(c.iglesiaId());
        }
        var usuario = usuarios.guardar(Usuario.crearPorAdmin(email, hasher.hashear(c.passwordTemporal()), c.nombres(),
                c.apellidos(), c.iglesiaId(), c.rol()));
        if (usuario.rol().tieneFinanzas()) {
            hogares.crearHogarInicial(usuario.id(), usuario.nombres(), "Hogar de " + usuario.nombres(), c.moneda());
        }
        return detalle(usuario);
    }

    @Override
    @Transactional
    public UsuarioDetalle actualizar(UUID actorId, UUID usuarioId, ComandoActualizar c) {
        exigirAdmin(actorId);
        var usuario = cargar(usuarioId);
        var nuevoRol = c.rol() == null ? usuario.rol() : c.rol();
        var nuevaIglesia = c.iglesiaId() == null ? usuario.iglesiaId() : c.iglesiaId();
        if (actorId.equals(usuarioId) && nuevoRol != usuario.rol()) {
            throw new DomainException("AUTO_CAMBIO_ROL", "No puedes cambiar tu propio rol");
        }
        if (usuario.rol() != RolUsuario.ADMIN && !Objects.equals(nuevaIglesia, usuario.iglesiaId())) {
            exigirIglesiaActiva(nuevaIglesia);
        }
        var cambiado = usuario
                .conDatos(c.nombres() == null ? usuario.nombres() : c.nombres(),
                        c.apellidos() == null ? usuario.apellidos() : c.apellidos())
                .conRolEIglesia(nuevoRol, nuevaIglesia);
        return detalle(usuarios.guardar(cambiado));
    }

    @Override
    @Transactional
    public UsuarioDetalle bloquear(UUID actorId, UUID usuarioId) {
        exigirAdmin(actorId);
        if (actorId.equals(usuarioId)) {
            throw new DomainException("AUTO_BLOQUEO", "No puedes bloquear tu propia cuenta");
        }
        return detalle(usuarios.guardar(cargar(usuarioId).bloquear()));
    }

    @Override
    @Transactional
    public UsuarioDetalle activar(UUID actorId, UUID usuarioId) {
        exigirAdmin(actorId);
        return detalle(usuarios.guardar(cargar(usuarioId).activar()));
    }

    @Override
    @Transactional
    public void resetearPassword(UUID actorId, UUID usuarioId, String passwordTemporal) {
        exigirAdmin(actorId);
        Usuario.validarPassword(passwordTemporal);
        usuarios.guardar(cargar(usuarioId).conPassword(hasher.hashear(passwordTemporal), true));
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenUsuarios resumen(UUID actorId) {
        exigirAdmin(actorId);
        var activos = new EnumMap<RolUsuario, Long>(RolUsuario.class);
        for (var rol : RolUsuario.values()) {
            activos.put(rol, 0L);
        }
        long bloqueados = 0;
        long total = 0;
        for (var conteo : usuarios.contarPorRolYEstado()) {
            total += conteo.cantidad();
            if (conteo.estado() == EstadoUsuario.ACTIVO) {
                activos.merge(conteo.rol(), conteo.cantidad(), Long::sum);
            } else if (conteo.estado() == EstadoUsuario.BLOQUEADO) {
                bloqueados += conteo.cantidad();
            }
        }
        return new ResumenUsuarios(Map.copyOf(activos), bloqueados, total);
    }

    private void exigirAdmin(UUID actorId) {
        var actor = usuarios.porId(actorId)
                .orElseThrow(() -> new AccesoDenegadoException("Cuenta no encontrada"));
        if (actor.rol() != RolUsuario.ADMIN || !actor.puedeIniciarSesion()) {
            throw new AccesoDenegadoException("Solo un administrador puede realizar esta acción");
        }
    }

    private void exigirIglesiaActiva(UUID iglesiaId) {
        if (iglesiaId == null) {
            throw new DomainException("IGLESIA_REQUERIDA", "Pastores y hermanos deben pertenecer a una iglesia");
        }
        if (!iglesias.existeActiva(iglesiaId)) {
            throw new DomainException("IGLESIA_INVALIDA", "La iglesia indicada no existe o no está activa");
        }
    }

    private Usuario cargar(UUID id) {
        return usuarios.porId(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    private UsuarioDetalle detalle(Usuario u) {
        String nombre = u.iglesiaId() == null ? null : iglesias.nombres(List.of(u.iglesiaId())).get(u.iglesiaId());
        return new UsuarioDetalle(u, nombre);
    }
}
