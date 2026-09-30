package com.selahfinance.hogar.domain.event;

import com.selahfinance.shared.domain.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/** Un hogar nuevo necesita sus categorías por defecto, etc. Publicado como {@code hogar.creado}. */
public record HogarCreado(UUID hogarId, UUID creadoPor, Instant ocurridoEn) implements DomainEvent {

    @Override
    public String tipo() {
        return "hogar.creado";
    }

    @Override
    public String agregado() {
        return "Hogar";
    }

    @Override
    public UUID agregadoId() {
        return hogarId;
    }
}
