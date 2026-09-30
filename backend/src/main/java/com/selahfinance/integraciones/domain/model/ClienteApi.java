package com.selahfinance.integraciones.domain.model;

import com.selahfinance.shared.domain.DomainException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Sistema de otro equipo autorizado a consumir nuestra API con una API key. */
public record ClienteApi(
        UUID id,
        String nombre,
        String descripcion,
        String responsableEmail,
        String apiKeyPrefijo,
        String apiKeyHash,
        Set<String> scopes,
        boolean activo,
        Instant expiraEn,
        Instant ultimoUsoAt) {

    /** Permisos que se pueden conceder. Se amplía a medida que se exponen endpoints a los otros equipos. */
    public static final Set<String> SCOPES_PERMITIDOS = Set.of(
            "movimientos:read", "movimientos:write", "habitos:write", "reportes:read");

    public ClienteApi {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("CLIENTE_NOMBRE_REQUERIDO", "El cliente necesita un nombre");
        }
        for (String scope : scopes) {
            if (!SCOPES_PERMITIDOS.contains(scope)) {
                throw new DomainException("SCOPE_INVALIDO",
                        "Scope no permitido: " + scope + ". Permitidos: " + SCOPES_PERMITIDOS);
            }
        }
    }

    public static ClienteApi nuevo(String nombre, String descripcion, String responsableEmail, Set<String> scopes,
            Instant expiraEn, ApiKey key) {
        return new ClienteApi(UUID.randomUUID(), nombre.trim(), descripcion, responsableEmail, key.prefijo(), key.hash(),
                Set.copyOf(scopes), true, expiraEn, null);
    }

    public ClienteApi revocar() {
        return new ClienteApi(id, nombre, descripcion, responsableEmail, apiKeyPrefijo, apiKeyHash, scopes, false,
                expiraEn, ultimoUsoAt);
    }

    public boolean vigente(Instant ahora) {
        return activo && (expiraEn == null || expiraEn.isAfter(ahora));
    }
}
