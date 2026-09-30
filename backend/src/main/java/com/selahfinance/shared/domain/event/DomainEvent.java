package com.selahfinance.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Hecho de negocio ya ocurrido. Se publica en memoria (otros módulos reaccionan)
 * y se guarda en el outbox para notificar a sistemas externos vía webhooks.
 */
public interface DomainEvent {

    /** Nombre estable del evento para integraciones, p. ej. {@code movimiento.registrado}. */
    String tipo();

    String agregado();

    UUID agregadoId();

    UUID hogarId();

    Instant ocurridoEn();
}
