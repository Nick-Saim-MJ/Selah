package com.selahfinance.integraciones.infrastructure.adapter;

import com.selahfinance.integraciones.application.port.out.WebhooksPorts.EnvioWebhookPort;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Envía el aviso firmado. Por seguridad (SSRF), una URL que apunte a la red interna (loopback, privada,
 * link-local) se rechaza salvo que {@code selah.webhooks.permitir-red-local=true} (solo desarrollo).
 */
@Component
class HttpEnvioWebhookAdapter implements EnvioWebhookPort {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final boolean permitirRedLocal;
    private final RestClient cliente;

    HttpEnvioWebhookAdapter(@Value("${selah.webhooks.permitir-red-local:false}") boolean permitirRedLocal) {
        this.permitirRedLocal = permitirRedLocal;
        var http = HttpClient.newBuilder().connectTimeout(TIMEOUT).followRedirects(HttpClient.Redirect.NEVER).build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(TIMEOUT);
        this.cliente = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public void enviar(String url, String cuerpo, String tipoEvento, UUID eventoId, String firma) {
        verificarDestino(URI.create(url));
        cliente.post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Selah-Evento", tipoEvento)
                .header("X-Selah-Evento-Id", eventoId.toString())
                .header("X-Selah-Firma", firma)
                .body(cuerpo)
                .retrieve()
                .toBodilessEntity(); // lanza si no es 2xx
    }

    private void verificarDestino(URI uri) {
        if (permitirRedLocal) {
            return;
        }
        try {
            for (InetAddress direccion : InetAddress.getAllByName(uri.getHost())) {
                if (direccion.isLoopbackAddress() || direccion.isSiteLocalAddress() || direccion.isLinkLocalAddress()
                        || direccion.isAnyLocalAddress()) {
                    throw new IllegalStateException("Destino no permitido (red interna): " + uri.getHost());
                }
            }
        } catch (UnknownHostException e) {
            throw new IllegalStateException("No se pudo resolver el destino: " + uri.getHost());
        }
    }
}
