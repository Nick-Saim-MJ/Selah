package com.selahfinance.integraciones.domain.model;

import com.selahfinance.shared.domain.DomainException;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

/** Un cliente API quiere que le avisemos (POST firmado) cuando ocurra cierto evento. */
public record SuscripcionWebhook(UUID id, UUID clienteApiId, String tipoEvento, String url, boolean activa) {

    /** Eventos que publicamos ({@code *} = todos). */
    public static final Set<String> EVENTOS = Set.of("*", "movimiento.registrado", "movimiento.eliminado", "hogar.creado");

    public SuscripcionWebhook {
        if (!EVENTOS.contains(tipoEvento)) {
            throw new DomainException("EVENTO_INVALIDO", "Evento no soportado. Disponibles: " + EVENTOS);
        }
        URI uri;
        try {
            uri = URI.create(url == null ? "" : url.trim());
        } catch (IllegalArgumentException e) {
            throw new DomainException("URL_INVALIDA", "La URL del webhook no es válida");
        }
        if (uri.getHost() == null || !("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))) {
            throw new DomainException("URL_INVALIDA", "La URL debe ser http(s) con un host válido");
        }
        url = uri.toString();
    }

    public static SuscripcionWebhook nueva(UUID clienteApiId, String tipoEvento, String url) {
        return new SuscripcionWebhook(UUID.randomUUID(), clienteApiId, tipoEvento, url, true);
    }
}
