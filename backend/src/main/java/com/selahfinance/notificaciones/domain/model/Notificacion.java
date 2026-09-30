package com.selahfinance.notificaciones.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Aviso para un usuario. Aparece en su bandeja a partir de {@code programadaPara}: normalmente
 * ahora, pero las de CONSUMO se posponen al atardecer del sábado si el Modo Sábado está activo.
 */
public record Notificacion(
        UUID id,
        UUID usuarioId,
        TipoNotificacion tipo,
        CategoriaNotificacion categoria,
        String titulo,
        String cuerpo,
        Map<String, String> datos,
        Instant programadaPara,
        Instant leidaAt,
        Instant creadaAt,
        String claveDedupe) {

    public static Notificacion nueva(UUID usuarioId, TipoNotificacion tipo, CategoriaNotificacion categoria,
            String titulo, String cuerpo, Map<String, String> datos, Instant programadaPara, Instant ahora,
            String claveDedupe) {
        return new Notificacion(UUID.randomUUID(), usuarioId, tipo, categoria, titulo, cuerpo,
                datos == null ? Map.of() : Map.copyOf(datos), programadaPara, null, ahora, claveDedupe);
    }

    public boolean visible(Instant ahora) {
        return !programadaPara.isAfter(ahora);
    }

    public boolean leida() {
        return leidaAt != null;
    }
}
