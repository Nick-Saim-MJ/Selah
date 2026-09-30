package com.selahfinance.notificaciones.application.usecase;

import com.selahfinance.notificaciones.application.port.in.ConsultarBandejaUseCase;
import com.selahfinance.notificaciones.application.port.in.GestionarPreferenciasUseCase;
import com.selahfinance.notificaciones.application.port.in.NotificarUseCase;
import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.NotificacionRepositoryPort;
import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.PoliticaSabadoPort;
import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.PreferenciasRepositoryPort;
import com.selahfinance.notificaciones.domain.model.Notificacion;
import com.selahfinance.notificaciones.domain.model.PreferenciasNotificacion;
import com.selahfinance.notificaciones.domain.service.PoliticaEntrega;
import com.selahfinance.shared.application.Pagina;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class NotificacionesService implements NotificarUseCase, ConsultarBandejaUseCase, GestionarPreferenciasUseCase {

    private static final int TAMANIO_MAXIMO = 50;

    private final NotificacionRepositoryPort notificaciones;
    private final PreferenciasRepositoryPort preferencias;
    private final PoliticaSabadoPort politicaSabado;
    private final Clock clock;

    @Override
    @Transactional
    public Optional<Notificacion> notificar(Comando c) {
        if (!obtener(c.usuarioId()).permite(c.tipo())) {
            return Optional.empty();
        }
        if (c.claveDedupe() != null && notificaciones.existeClave(c.usuarioId(), c.claveDedupe())) {
            return Optional.empty();
        }
        var ahora = ZonedDateTime.now(clock);
        boolean modoSabado = c.hogarId() != null && politicaSabado.modoSabadoActivo(c.hogarId());
        Instant programada = PoliticaEntrega.programar(c.categoria(), ahora, modoSabado);
        return Optional.of(notificaciones.guardar(Notificacion.nueva(c.usuarioId(), c.tipo(), c.categoria(),
                c.titulo(), c.cuerpo(), c.datos(), programada, ahora.toInstant(), c.claveDedupe())));
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<Notificacion> bandeja(UUID usuarioId, int pagina, int tamanio) {
        return notificaciones.visibles(usuarioId, clock.instant(), Math.max(pagina, 0),
                Math.min(Math.max(tamanio, 1), TAMANIO_MAXIMO));
    }

    @Override
    @Transactional(readOnly = true)
    public long noLeidas(UUID usuarioId) {
        return notificaciones.contarNoLeidasVisibles(usuarioId, clock.instant());
    }

    @Override
    @Transactional
    public void marcarLeida(UUID usuarioId, UUID notificacionId) {
        var n = notificaciones.porId(usuarioId, notificacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación", notificacionId));
        if (!n.leida()) {
            notificaciones.guardar(new Notificacion(n.id(), n.usuarioId(), n.tipo(), n.categoria(), n.titulo(),
                    n.cuerpo(), n.datos(), n.programadaPara(), clock.instant(), n.creadaAt(), n.claveDedupe()));
        }
    }

    @Override
    @Transactional
    public void marcarTodasLeidas(UUID usuarioId) {
        notificaciones.marcarTodasLeidas(usuarioId, clock.instant());
    }

    @Override
    @Transactional(readOnly = true)
    public PreferenciasNotificacion obtener(UUID usuarioId) {
        return preferencias.de(usuarioId).orElseGet(() -> PreferenciasNotificacion.porDefecto(usuarioId));
    }

    @Override
    @Transactional
    public PreferenciasNotificacion actualizar(PreferenciasNotificacion p) {
        return preferencias.guardar(p);
    }
}
