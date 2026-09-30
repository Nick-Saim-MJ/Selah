package com.selahfinance.shared.infrastructure.web;

import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Identidad extraída del JWT emitido por el módulo identidad.
 * {@code hogarId} es null para ADMIN (no tiene finanzas); {@code iglesiaId} es null solo para ADMIN.
 */
public record UsuarioActual(UUID usuarioId, UUID hogarId, UUID iglesiaId, String rol) {

    public static final String CLAIM_HOGAR = "hogar_id";
    public static final String CLAIM_IGLESIA = "iglesia_id";
    public static final String CLAIM_ROL = "rol";

    public static UsuarioActual de(Jwt jwt) {
        return new UsuarioActual(
                UUID.fromString(jwt.getSubject()),
                uuidODefault(jwt.getClaimAsString(CLAIM_HOGAR)),
                uuidODefault(jwt.getClaimAsString(CLAIM_IGLESIA)),
                jwt.getClaimAsString(CLAIM_ROL));
    }

    private static UUID uuidODefault(String valor) {
        return valor == null ? null : UUID.fromString(valor);
    }
}
