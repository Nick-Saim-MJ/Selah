package com.selahfinance.shared.infrastructure.event;

import com.selahfinance.shared.domain.event.DomainEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Guarda cada evento de dominio en {@code evento_outbox} dentro de la misma transacción
 * del caso de uso. El publicador de webhooks (módulo integraciones) los enviará luego
 * a los sistemas de otros equipos suscritos.
 */
@Component
@RequiredArgsConstructor
class OutboxEventListener {

    private final OutboxEventoJpaRepository repository;
    private final JsonMapper jsonMapper;

    @EventListener
    void alOcurrir(DomainEvent evento) {
        var fila = new OutboxEventoJpaEntity();
        fila.setId(UUID.randomUUID());
        fila.setTipoEvento(evento.tipo());
        fila.setAgregado(evento.agregado());
        fila.setAgregadoId(evento.agregadoId());
        fila.setHogarId(evento.hogarId());
        fila.setPayload(jsonMapper.writeValueAsString(evento));
        fila.setEstado("PENDIENTE");
        fila.setOcurridoAt(evento.ocurridoEn());
        repository.save(fila);
    }
}
