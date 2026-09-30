package com.selahfinance.habitos.infrastructure.adapter;

import com.selahfinance.habitos.application.port.out.HabitoRepositoryPort;
import com.selahfinance.reportes.application.port.out.MetricasDimensionPort;
import com.selahfinance.reportes.domain.model.CumplimientoHabito;
import com.selahfinance.shared.domain.DimensionMayordomia;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Fuente LOCAL de métricas 4T (lo que el usuario registra en la app). Coexiste con los adaptadores
 * de APIs de otros equipos: el reporte combina todas las fuentes.
 *
 * <p>Si el usuario no registró nada de la dimensión en el rango, devuelve vacío (sin datos) para no
 * castigar con 0% a quien nunca usó esa parte; si registró algo, se mide contra TODAS las metas de
 * la dimensión (no solo contra las que anotó).
 */
@Component
@RequiredArgsConstructor
class MetricasLocalesAdapter implements MetricasDimensionPort {

    private final HabitoRepositoryPort habitos;

    @Override
    public List<CumplimientoHabito> obtener(UUID usuarioId, DimensionMayordomia dimension, LocalDate desde,
            LocalDate hasta) {
        var delaDimension = habitos.activos(null).stream().filter(h -> h.dimension() == dimension).toList();
        var sumas = habitos.sumasPorHabito(usuarioId, desde, hasta);
        boolean tieneDatos = delaDimension.stream().anyMatch(h -> sumas.containsKey(h.id()));
        if (!tieneDatos) {
            return List.of();
        }
        return delaDimension.stream()
                .map(h -> new CumplimientoHabito(h.codigo(), h.nombre(), h.metaParaRango(desde, hasta),
                        sumas.getOrDefault(h.id(), BigDecimal.ZERO), "APP"))
                .toList();
    }
}
