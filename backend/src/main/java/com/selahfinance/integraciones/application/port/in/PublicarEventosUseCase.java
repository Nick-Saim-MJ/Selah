package com.selahfinance.integraciones.application.port.in;

public interface PublicarEventosUseCase {

    /**
     * Envía los eventos pendientes del outbox a los webhooks suscritos. Un evento se reintenta hasta
     * {@code MAX_INTENTOS} veces; los receptores deben ser idempotentes (header X-Selah-Evento-Id).
     * Devuelve cuántos eventos quedaron publicados.
     */
    int publicarPendientes();

    int MAX_INTENTOS = 5;
}
