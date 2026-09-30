package com.selahfinance.notificaciones.infrastructure.persistence;

import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.NotificacionRepositoryPort;
import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.PreferenciasRepositoryPort;
import com.selahfinance.notificaciones.domain.model.CategoriaNotificacion;
import com.selahfinance.notificaciones.domain.model.Notificacion;
import com.selahfinance.notificaciones.domain.model.PreferenciasNotificacion;
import com.selahfinance.notificaciones.domain.model.TipoNotificacion;
import com.selahfinance.shared.application.Pagina;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
class NotificacionesPersistenceAdapter implements NotificacionRepositoryPort, PreferenciasRepositoryPort {

    private static final TypeReference<Map<String, String>> MAPA = new TypeReference<>() {
    };

    private final NotificacionJpaRepository notificaciones;
    private final PreferenciaNotificacionJpaRepository preferencias;
    private final JsonMapper json;

    // ---------- Notificaciones

    @Override
    public Notificacion guardar(Notificacion n) {
        var e = notificaciones.findById(n.id()).orElseGet(NotificacionJpaEntity::new);
        e.setId(n.id());
        e.setUsuarioId(n.usuarioId());
        e.setTipo(n.tipo().name());
        e.setCategoria(n.categoria().name());
        e.setTitulo(n.titulo());
        e.setCuerpo(n.cuerpo());
        e.setDatos(n.datos().isEmpty() ? null : json.writeValueAsString(n.datos()));
        e.setProgramadaPara(n.programadaPara());
        e.setLeidaAt(n.leidaAt());
        e.setCreadaAt(n.creadaAt());
        e.setClaveDedupe(n.claveDedupe());
        return aDominio(notificaciones.saveAndFlush(e));
    }

    @Override
    public boolean existeClave(UUID usuarioId, String claveDedupe) {
        return notificaciones.existsByUsuarioIdAndClaveDedupe(usuarioId, claveDedupe);
    }

    @Override
    public Pagina<Notificacion> visibles(UUID usuarioId, Instant ahora, int pagina, int tamanio) {
        var resultado = notificaciones.findByUsuarioIdAndProgramadaParaLessThanEqualOrderByProgramadaParaDesc(usuarioId,
                ahora, PageRequest.of(pagina, tamanio));
        return new Pagina<>(resultado.getContent().stream().map(this::aDominio).toList(), pagina, tamanio,
                resultado.getTotalElements());
    }

    @Override
    public long contarNoLeidasVisibles(UUID usuarioId, Instant ahora) {
        return notificaciones.countByUsuarioIdAndLeidaAtIsNullAndProgramadaParaLessThanEqual(usuarioId, ahora);
    }

    @Override
    public Optional<Notificacion> porId(UUID usuarioId, UUID notificacionId) {
        return notificaciones.findByIdAndUsuarioId(notificacionId, usuarioId).map(this::aDominio);
    }

    @Override
    public void marcarTodasLeidas(UUID usuarioId, Instant ahora) {
        notificaciones.marcarTodasLeidas(usuarioId, ahora);
    }

    // ---------- Preferencias

    @Override
    public Optional<PreferenciasNotificacion> de(UUID usuarioId) {
        return preferencias.findById(usuarioId).map(NotificacionesPersistenceAdapter::aDominio);
    }

    @Override
    public PreferenciasNotificacion guardar(PreferenciasNotificacion p) {
        var e = preferencias.findById(p.usuarioId()).orElseGet(PreferenciaNotificacionJpaEntity::new);
        e.setUsuarioId(p.usuarioId());
        e.setRecordatorioDiezmo(p.recordatorioDiezmo());
        e.setRecordatorioDeudas(p.recordatorioDeudas());
        e.setResumenSemanal(p.resumenSemanal());
        e.setReflexionSabado(p.reflexionSabado());
        return aDominio(preferencias.saveAndFlush(e));
    }

    private Notificacion aDominio(NotificacionJpaEntity e) {
        Map<String, String> datos = e.getDatos() == null ? Map.of() : json.readValue(e.getDatos(), MAPA);
        return new Notificacion(e.getId(), e.getUsuarioId(), TipoNotificacion.valueOf(e.getTipo()),
                CategoriaNotificacion.valueOf(e.getCategoria()), e.getTitulo(), e.getCuerpo(), datos,
                e.getProgramadaPara(), e.getLeidaAt(), e.getCreadaAt(), e.getClaveDedupe());
    }

    private static PreferenciasNotificacion aDominio(PreferenciaNotificacionJpaEntity e) {
        return new PreferenciasNotificacion(e.getUsuarioId(), e.isRecordatorioDiezmo(), e.isRecordatorioDeudas(),
                e.isResumenSemanal(), e.isReflexionSabado());
    }
}
