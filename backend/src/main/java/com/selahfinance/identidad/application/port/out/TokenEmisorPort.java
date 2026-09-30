package com.selahfinance.identidad.application.port.out;

import com.selahfinance.identidad.domain.model.RolUsuario;
import java.time.Instant;
import java.util.UUID;

public interface TokenEmisorPort {

    /** hogarId e iglesiaId pueden ser null (ADMIN). */
    TokenEmitido emitir(UUID usuarioId, UUID hogarId, UUID iglesiaId, RolUsuario rol);

    record TokenEmitido(String token, Instant expiraEn) {
    }
}
