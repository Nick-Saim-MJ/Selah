package com.selahfinance.integraciones.infrastructure.adapter;

import com.selahfinance.integraciones.application.port.out.WebhooksPorts.Destino;
import com.selahfinance.integraciones.application.port.out.WebhooksPorts.OutboxPort;
import com.selahfinance.integraciones.application.port.out.WebhooksPorts.SuscripcionRepositoryPort;
import com.selahfinance.integraciones.domain.model.EventoOutbox;
import com.selahfinance.integraciones.domain.model.SuscripcionWebhook;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Outbox (escrito por shared) y suscripciones, con SQL directo: son tablas de infraestructura. */
@Component
@RequiredArgsConstructor
class WebhooksJdbcAdapter implements OutboxPort, SuscripcionRepositoryPort {

    private final JdbcClient jdbc;

    // ---------- Outbox

    @Override
    public List<EventoOutbox> pendientes(int limite) {
        return jdbc.sql("""
                        select id, tipo_evento, agregado, agregado_id, hogar_id, payload::text, intentos, ocurrido_at
                        from evento_outbox where estado = 'PENDIENTE'
                        order by ocurrido_at limit ?""")
                .param(limite)
                .query((rs, n) -> new EventoOutbox(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3),
                        rs.getObject(4, UUID.class), rs.getObject(5, UUID.class), rs.getString(6), rs.getInt(7),
                        rs.getTimestamp(8).toInstant()))
                .list();
    }

    @Override
    public void marcarPublicado(UUID eventoId, Instant momento) {
        jdbc.sql("update evento_outbox set estado = 'PUBLICADO', publicado_at = ? where id = ?")
                .params(Timestamp.from(momento), eventoId).update();
    }

    @Override
    public void registrarFallo(UUID eventoId, String error, int maxIntentos) {
        jdbc.sql("""
                        update evento_outbox
                        set intentos = intentos + 1, ultimo_error = ?,
                            estado = case when intentos + 1 >= ? then 'FALLIDO' else 'PENDIENTE' end
                        where id = ?""")
                .params(error, maxIntentos, eventoId).update();
    }

    // ---------- Suscripciones

    @Override
    public void guardar(SuscripcionWebhook s, String secretoCifrado, Instant creadaEn) {
        // created_at con el reloj de la app (no now() de la BD) para compararlo con ocurrido_at de los eventos
        jdbc.sql("""
                        insert into suscripcion_webhook (id, cliente_api_id, tipo_evento, url_destino, secreto_cifrado, activa, created_at)
                        values (?, ?, ?, ?, ?, ?, ?)""")
                .params(s.id(), s.clienteApiId(), s.tipoEvento(), s.url(), secretoCifrado, s.activa(), Timestamp.from(creadaEn))
                .update();
    }

    @Override
    public List<SuscripcionWebhook> todas() {
        return jdbc.sql("select id, cliente_api_id, tipo_evento, url_destino, activa from suscripcion_webhook order by created_at")
                .query((rs, n) -> new SuscripcionWebhook(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class),
                        rs.getString(3), rs.getString(4), rs.getBoolean(5)))
                .list();
    }

    @Override
    public Optional<SuscripcionWebhook> porId(UUID id) {
        return jdbc.sql("select id, cliente_api_id, tipo_evento, url_destino, activa from suscripcion_webhook where id = ?")
                .param(id)
                .query((rs, n) -> new SuscripcionWebhook(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class),
                        rs.getString(3), rs.getString(4), rs.getBoolean(5)))
                .optional();
    }

    @Override
    public void eliminar(UUID id) {
        jdbc.sql("delete from suscripcion_webhook where id = ?").param(id).update();
    }

    @Override
    public List<Destino> activasParaEvento(String tipoEvento, Instant ocurridoEn) {
        return jdbc.sql("""
                        select s.id, s.url_destino, s.secreto_cifrado
                        from suscripcion_webhook s
                        join cliente_api c on c.id = s.cliente_api_id
                        where s.activa and c.activo and (c.expira_at is null or c.expira_at > now())
                          and s.tipo_evento in (?, '*') and s.created_at <= ?""")
                .params(tipoEvento, Timestamp.from(ocurridoEn))
                .query((rs, n) -> new Destino(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3)))
                .list();
    }
}
