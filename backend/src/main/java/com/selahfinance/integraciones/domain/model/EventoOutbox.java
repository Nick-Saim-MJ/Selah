package com.selahfinance.integraciones.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Evento de dominio guardado en la misma transacción que el cambio, pendiente de avisar a los webhooks. */
public record EventoOutbox(UUID id, String tipoEvento, String agregado, UUID agregadoId, UUID hogarId, String payload,
        int intentos, Instant ocurridoEn) {
}
