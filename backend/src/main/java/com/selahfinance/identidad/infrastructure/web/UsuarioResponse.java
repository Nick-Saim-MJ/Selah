package com.selahfinance.identidad.infrastructure.web;

import com.selahfinance.identidad.application.dto.UsuarioDetalle;
import com.selahfinance.identidad.domain.model.EstadoUsuario;
import com.selahfinance.identidad.domain.model.RolUsuario;
import java.util.UUID;

/** Vista de una cuenta. Nunca incluye el hash de contraseña. */
record UsuarioResponse(
        UUID id,
        String email,
        String nombres,
        String apellidos,
        RolUsuario rol,
        EstadoUsuario estado,
        UUID iglesiaId,
        String iglesiaNombre,
        boolean debeCambiarPassword,
        boolean onboardingCompletado,
        boolean comparteReporte) {

    static UsuarioResponse de(UsuarioDetalle d) {
        var u = d.usuario();
        return new UsuarioResponse(u.id(), u.email(), u.nombres(), u.apellidos(), u.rol(), u.estado(), u.iglesiaId(),
                d.iglesiaNombre(), u.debeCambiarPassword(), u.onboardingCompletado(), u.comparteReporte());
    }
}
