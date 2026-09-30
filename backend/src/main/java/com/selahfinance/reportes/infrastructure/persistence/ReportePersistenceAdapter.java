package com.selahfinance.reportes.infrastructure.persistence;

import com.selahfinance.reportes.application.port.out.ReporteRepositoryPort;
import com.selahfinance.reportes.domain.model.CumplimientoHabito;
import com.selahfinance.reportes.domain.model.PuntajesMayordomia;
import com.selahfinance.reportes.domain.model.ReporteMayordomia;
import com.selahfinance.shared.domain.DimensionMayordomia;
import com.selahfinance.shared.domain.Periodo;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Guarda la foto del reporte. El desglose de hábitos y el "diezmo al día" van en la columna JSON
 * {@code detalle}; los puntajes en columnas para poder consultarlos con SQL.
 */
@Component
@RequiredArgsConstructor
class ReportePersistenceAdapter implements ReporteRepositoryPort {

    private static final String MENSUAL = "MENSUAL";

    private final ReporteMayordomiaJpaRepository repository;
    private final JsonMapper json;

    @Override
    public void guardar(ReporteMayordomia r) {
        var e = repository.findByHogarIdAndUsuarioIdAndTipoPeriodoAndPeriodoInicio(
                r.hogarId(), r.usuarioId(), MENSUAL, r.periodo().inicio()).orElseGet(() -> {
                    var nuevo = new ReporteMayordomiaJpaEntity();
                    nuevo.setId(UUID.randomUUID());
                    nuevo.setHogarId(r.hogarId());
                    nuevo.setUsuarioId(r.usuarioId());
                    nuevo.setTipoPeriodo(MENSUAL);
                    nuevo.setPeriodoInicio(r.periodo().inicio());
                    return nuevo;
                });
        var p = r.puntajes();
        e.setPeriodoFin(r.periodo().fin());
        e.setPuntajeTiempo(p.tiempo());
        e.setPuntajeTalento(p.talento());
        e.setPuntajeTesoro(p.tesoro());
        e.setPuntajeTemplo(p.templo());
        e.setPuntajeGlobal(p.global().orElse(null));
        e.setSemaforo(r.semaforo().map(Enum::name).orElse(null));
        e.setDetalle(json.writeValueAsString(aJson(r)));
        e.setVersionAlgoritmo(r.versionAlgoritmo());
        e.setGeneradoAt(r.generadoEn());
        repository.saveAndFlush(e);
    }

    @Override
    public Optional<ReporteMayordomia> porUsuarioYPeriodo(UUID usuarioId, UUID hogarId, Periodo periodo) {
        return repository.findByHogarIdAndUsuarioIdAndTipoPeriodoAndPeriodoInicio(hogarId, usuarioId, MENSUAL,
                periodo.inicio()).map(this::aDominio);
    }

    private ReporteMayordomia aDominio(ReporteMayordomiaJpaEntity e) {
        var detalle = json.readValue(e.getDetalle(), DetalleJson.class);
        var habitos = new EnumMap<DimensionMayordomia, List<CumplimientoHabito>>(DimensionMayordomia.class);
        if (detalle.habitos() != null) {
            detalle.habitos().forEach((dimension, lista) -> habitos.put(DimensionMayordomia.valueOf(dimension),
                    lista.stream().map(h -> new CumplimientoHabito(h.codigo(), h.nombre(), h.meta(), h.valor(),
                            h.fuente())).toList()));
        }
        return new ReporteMayordomia(e.getUsuarioId(), e.getHogarId(), new Periodo(YearMonth.from(e.getPeriodoInicio())),
                new PuntajesMayordomia(e.getPuntajeTiempo(), e.getPuntajeTalento(), e.getPuntajeTesoro(),
                        e.getPuntajeTemplo()),
                Map.copyOf(habitos), detalle.diezmoAlDia(), e.getGeneradoAt(), e.getVersionAlgoritmo());
    }

    private static DetalleJson aJson(ReporteMayordomia r) {
        var habitos = new EnumMap<DimensionMayordomia, List<HabitoJson>>(DimensionMayordomia.class);
        r.habitos().forEach((dimension, lista) -> habitos.put(dimension, lista.stream()
                .map(h -> new HabitoJson(h.codigo(), h.nombre(), h.metaPeriodo(), h.valorRegistrado(), h.fuente()))
                .toList()));
        var claves = new java.util.LinkedHashMap<String, List<HabitoJson>>();
        habitos.forEach((d, l) -> claves.put(d.name(), l));
        return new DetalleJson(claves, r.diezmoAlDia());
    }

    /** Contrato de la columna {@code detalle}. Nunca contiene montos de dinero. */
    record DetalleJson(Map<String, List<HabitoJson>> habitos, Boolean diezmoAlDia) {
    }

    record HabitoJson(String codigo, String nombre, BigDecimal meta, BigDecimal valor, String fuente) {
    }
}
