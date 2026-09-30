package com.selahfinance.identidad.application.dto;

import com.selahfinance.identidad.domain.model.RolUsuario;
import java.time.Instant;
import java.util.UUID;

/** hogarId es null para ADMIN; iglesiaId es null solo para ADMIN. */
public record SesionResult(
        String accessToken,
        Instant expiraEn,
        UUID usuarioId,
        UUID hogarId,
        UUID iglesiaId,
        RolUsuario rol,
        String nombres,
        boolean onboardingCompletado,
        boolean debeCambiarPassword) {
}
