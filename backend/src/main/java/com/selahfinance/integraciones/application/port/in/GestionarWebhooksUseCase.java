package com.selahfinance.integraciones.application.port.in;

import com.selahfinance.integraciones.domain.model.SuscripcionWebhook;
import java.util.List;
import java.util.UUID;

public interface GestionarWebhooksUseCase {

    /** Crea la suscripción. El secreto de firma solo se devuelve aquí, una única vez. */
    WebhookCreado crear(UUID clienteApiId, String tipoEvento, String url);

    List<SuscripcionWebhook> listarSuscripciones();

    void eliminar(UUID suscripcionId);

    record WebhookCreado(SuscripcionWebhook suscripcion, String secreto) {
    }
}
