package com.selahfinance.integraciones.infrastructure.adapter;

import com.selahfinance.reportes.application.port.out.MetricasDimensionPort;
import com.selahfinance.reportes.domain.model.CumplimientoHabito;
import com.selahfinance.shared.domain.DimensionMayordomia;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Consume las APIs de los módulos de otros equipos (tabla proveedor_externo) que aportan
 * métricas de Tiempo, Talento o Templo. Contrato esperado en docs/integracion-apis.md:
 *
 * <pre>
 * GET {base_url}/api/v1/mayordomia/metricas?usuarioId={idExterno}&amp;dimension=TEMPLO&amp;desde=2026-09-01&amp;hasta=2026-09-30
 * X-API-Key: ${SELAH_PROVEEDOR_&lt;CODIGO&gt;_SECRET}
 * → [{ "codigo": "EJERCICIO", "metaPeriodo": 900, "valorRegistrado": 640 }]
 * </pre>
 *
 * Tolerante a fallos: si un proveedor no responde, se registra en el log y se omite.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class HttpMetricasDimensionAdapter implements MetricasDimensionPort {

    private final JdbcClient jdbc;
    private final RestClient.Builder restClientBuilder;
    private final Environment env;

    @Override
    public List<CumplimientoHabito> obtener(UUID usuarioId, DimensionMayordomia dimension, LocalDate desde,
            LocalDate hasta) {
        var proveedores = jdbc.sql("""
                        select p.codigo, p.base_url, p.tipo_auth, p.timeout_ms, v.id_externo
                        from proveedor_externo p
                        join vinculo_usuario_externo v on v.proveedor_id = p.id and v.usuario_id = ?
                        where p.activo and p.dimension = ?""")
                .params(usuarioId, dimension.name())
                .query((rs, n) -> new Proveedor(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4),
                        rs.getString(5)))
                .list();

        var resultado = new ArrayList<CumplimientoHabito>();
        for (var p : proveedores) {
            try {
                var metricas = cliente(p).get()
                        .uri(u -> u.path("/api/v1/mayordomia/metricas")
                                .queryParam("usuarioId", p.idExterno())
                                .queryParam("dimension", dimension.name())
                                .queryParam("desde", desde)
                                .queryParam("hasta", hasta)
                                .build())
                        .retrieve()
                        .body(MetricaExterna[].class);
                if (metricas != null) {
                    for (var m : metricas) {
                        resultado.add(new CumplimientoHabito(m.codigo(), m.codigo(), m.metaPeriodo(), m.valorRegistrado(), p.codigo()));
                    }
                }
            } catch (RestClientException e) {
                log.warn("Proveedor externo {} no disponible para {}: {}", p.codigo(), dimension, e.getMessage());
            }
        }
        return resultado;
    }

    private RestClient cliente(Proveedor p) {
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(p.timeoutMs())).build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofMillis(p.timeoutMs()));
        var builder = restClientBuilder.clone().baseUrl(p.baseUrl()).requestFactory(factory);
        String secreto = env.getProperty("SELAH_PROVEEDOR_" + p.codigo().toUpperCase(Locale.ROOT) + "_SECRET");
        if (secreto != null) {
            switch (p.tipoAuth()) {
                case "API_KEY" -> builder.defaultHeader("X-API-Key", secreto);
                case "BEARER" -> builder.defaultHeader("Authorization", "Bearer " + secreto);
                default -> { }
            }
        }
        return builder.build();
    }

    private record Proveedor(String codigo, String baseUrl, String tipoAuth, int timeoutMs, String idExterno) {
    }

    record MetricaExterna(String codigo, BigDecimal metaPeriodo, BigDecimal valorRegistrado) {
    }
}
