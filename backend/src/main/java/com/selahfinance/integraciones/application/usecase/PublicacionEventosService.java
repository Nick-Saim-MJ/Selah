package com.selahfinance.integraciones.application.usecase;

import com.selahfinance.integraciones.application.port.in.PublicarEventosUseCase;
import com.selahfinance.integraciones.application.port.out.WebhooksPorts.CifradorPort;
import com.selahfinance.integraciones.application.port.out.WebhooksPorts.EnvioWebhookPort;
import com.selahfinance.integraciones.application.port.out.WebhooksPorts.OutboxPort;
import com.selahfinance.integraciones.application.port.out.WebhooksPorts.SuscripcionRepositoryPort;
import com.selahfinance.integraciones.domain.service.FirmaWebhook;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Publica el outbox. No es transaccional en conjunto a propósito: cada evento se marca por su cuenta,
 * de modo que un destino caído no deshace los ya enviados. Si un envío falla, el evento se reintenta
 * completo (los receptores deben ser idempotentes).
 */
@Slf4j
@Service
@RequiredArgsConstructor
class PublicacionEventosService implements PublicarEventosUseCase {

    private static final int LOTE = 50;

    private final OutboxPort outbox;
    private final SuscripcionRepositoryPort suscripciones;
    private final CifradorPort cifrador;
    private final EnvioWebhookPort envio;
    private final Clock clock;

    @Override
    public int publicarPendientes() {
        int publicados = 0;
        for (var evento : outbox.pendientes(LOTE)) {
            String fallo = null;
            for (var destino : suscripciones.activasParaEvento(evento.tipoEvento(), evento.ocurridoEn())) {
                try {
                    String firma = FirmaWebhook.firmar(cifrador.descifrar(destino.secretoCifrado()), evento.payload());
                    envio.enviar(destino.url(), evento.payload(), evento.tipoEvento(), evento.id(), firma);
                } catch (RuntimeException e) {
                    fallo = "Webhook " + destino.suscripcionId() + ": " + e.getMessage();
                    log.warn("Falló el aviso de {} a {}: {}", evento.tipoEvento(), destino.url(), e.getMessage());
                }
            }
            if (fallo == null) {
                outbox.marcarPublicado(evento.id(), clock.instant());
                publicados++;
            } else {
                outbox.registrarFallo(evento.id(), fallo, MAX_INTENTOS);
            }
        }
        return publicados;
    }
}
