package com.selahfinance.shared.application.port.out;

import com.selahfinance.shared.domain.event.DomainEvent;

/** Publica eventos de dominio dentro de la transacción actual. */
public interface EventPublisherPort {

    void publicar(DomainEvent evento);
}
