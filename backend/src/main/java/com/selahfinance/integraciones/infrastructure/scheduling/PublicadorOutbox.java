package com.selahfinance.integraciones.infrastructure.scheduling;

import com.selahfinance.integraciones.application.port.in.PublicarEventosUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class PublicadorOutbox {

    private final PublicarEventosUseCase publicador;

    @Scheduled(fixedDelayString = "${selah.webhooks.intervalo-ms:15000}", initialDelayString = "${selah.webhooks.intervalo-ms:15000}")
    void publicar() {
        int publicados = publicador.publicarPendientes();
        if (publicados > 0) {
            log.debug("Eventos publicados a webhooks: {}", publicados);
        }
    }
}
