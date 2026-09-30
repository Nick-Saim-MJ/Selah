package com.selahfinance.integraciones.application.port.out;

import com.selahfinance.integraciones.domain.model.EventoOutbox;
import com.selahfinance.integraciones.domain.model.SuscripcionWebhook;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puertos de salida para webhooks y outbox. */
public final class WebhooksPorts {

    private WebhooksPorts() {
    }

    public interface SuscripcionRepositoryPort {

        void guardar(SuscripcionWebhook suscripcion, String secretoCifrado, Instant creadaEn);

        List<SuscripcionWebhook> todas();

        Optional<SuscripcionWebhook> porId(UUID id);

        void eliminar(UUID id);

        /**
         * Suscripciones activas (de clientes vigentes) que quieren este evento o {@code *} y que ya existían
         * cuando ocurrió: una suscripción nueva no recibe el historial pendiente.
         */
        List<Destino> activasParaEvento(String tipoEvento, Instant ocurridoEn);
    }

    public record Destino(UUID suscripcionId, String url, String secretoCifrado) {
    }

    public interface OutboxPort {

        List<EventoOutbox> pendientes(int limite);

        void marcarPublicado(UUID eventoId, Instant momento);

        /** Suma un intento; pasa a FALLIDO al llegar a {@code maxIntentos}. */
        void registrarFallo(UUID eventoId, String error, int maxIntentos);
    }

    public interface CifradorPort {

        String cifrar(String textoPlano);

        String descifrar(String textoCifrado);
    }

    public interface EnvioWebhookPort {

        /** Hace el POST; lanza si el destino no responde 2xx o no es una dirección permitida. */
        void enviar(String url, String cuerpo, String tipoEvento, UUID eventoId, String firma);
    }
}
