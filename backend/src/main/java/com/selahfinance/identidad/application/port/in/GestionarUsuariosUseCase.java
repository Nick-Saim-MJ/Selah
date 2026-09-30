package com.selahfinance.identidad.application.port.in;

import com.selahfinance.identidad.application.dto.UsuarioDetalle;
import com.selahfinance.identidad.domain.model.EstadoUsuario;
import com.selahfinance.identidad.domain.model.RolUsuario;
import com.selahfinance.shared.application.Pagina;
import java.util.Map;
import java.util.UUID;

/** Administración de cuentas. Todas las operaciones exigen que {@code actorId} sea un ADMIN activo. */
public interface GestionarUsuariosUseCase {

    Pagina<UsuarioDetalle> listar(UUID actorId, Filtro filtro);

    UsuarioDetalle crear(UUID actorId, ComandoCrear comando);

    UsuarioDetalle actualizar(UUID actorId, UUID usuarioId, ComandoActualizar comando);

    UsuarioDetalle bloquear(UUID actorId, UUID usuarioId);

    UsuarioDetalle activar(UUID actorId, UUID usuarioId);

    /** Asigna una contraseña temporal; el usuario deberá cambiarla al ingresar. */
    void resetearPassword(UUID actorId, UUID usuarioId, String passwordTemporal);

    ResumenUsuarios resumen(UUID actorId);

    record Filtro(RolUsuario rol, UUID iglesiaId, EstadoUsuario estado, String texto, int pagina, int tamanio) {
    }

    record ComandoCrear(
            String email,
            String nombres,
            String apellidos,
            RolUsuario rol,
            UUID iglesiaId,
            String passwordTemporal,
            String moneda) {
    }

    record ComandoActualizar(String nombres, String apellidos, RolUsuario rol, UUID iglesiaId) {
    }

    /** Conteos por rol y estado para el panel del admin. */
    record ResumenUsuarios(Map<RolUsuario, Long> activosPorRol, long bloqueados, long total) {
    }
}
