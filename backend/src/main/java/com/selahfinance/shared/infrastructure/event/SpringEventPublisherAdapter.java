package com.selahfinance.shared.infrastructure.event;

import com.selahfinance.shared.application.port.out.EventPublisherPort;
import com.selahfinance.shared.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SpringEventPublisherAdapter implements EventPublisherPort {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publicar(DomainEvent evento) {
        publisher.publishEvent(evento);
    }
}
