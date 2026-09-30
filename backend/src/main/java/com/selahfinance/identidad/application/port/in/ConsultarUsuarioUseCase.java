package com.selahfinance.identidad.application.port.in;

import com.selahfinance.identidad.domain.model.RolUsuario;
import com.selahfinance.identidad.domain.model.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** API pública de identidad para otros módulos (p. ej. congregación, filtro de seguridad). */
public interface ConsultarUsuarioUseCase {

    Optional<Usuario> porId(UUID id);

    /** ¿La cuenta sigue ACTIVA y con el rol que dice el token? Permite bloqueos y cambios de rol inmediatos. */
    boolean sesionVigente(UUID usuarioId, RolUsuario rolDelToken);

    /** Hermanos activos de una iglesia (sin pastores ni admins). */
    List<Usuario> hermanosActivosDe(UUID iglesiaId);
}
