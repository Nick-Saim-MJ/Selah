package com.selahfinance.reportes.domain.model;

import com.selahfinance.shared.domain.DimensionMayordomia;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Puntajes 0–100 de las cuatro dimensiones (Tiempo, Talento, Tesoro, Templo).
 * Una dimensión sin datos en el periodo es {@code null} y no penaliza el global.
 */
public record PuntajesMayordomia(BigDecimal tiempo, BigDecimal talento, BigDecimal tesoro, BigDecimal templo) {

    public Map<DimensionMayordomia, BigDecimal> porDimension() {
        var mapa = new EnumMap<DimensionMayordomia, BigDecimal>(DimensionMayordomia.class);
        mapa.put(DimensionMayordomia.TIEMPO, tiempo);
        mapa.put(DimensionMayordomia.TALENTO, talento);
        mapa.put(DimensionMayordomia.TESORO, tesoro);
        mapa.put(DimensionMayordomia.TEMPLO, templo);
        return mapa;
    }

    /** Promedio simple de las dimensiones con datos. */
    public Optional<BigDecimal> global() {
        var conDatos = porDimension().values().stream().filter(Objects::nonNull).toList();
        if (conDatos.isEmpty()) {
            return Optional.empty();
        }
        var suma = conDatos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return Optional.of(suma.divide(BigDecimal.valueOf(conDatos.size()), 2, RoundingMode.HALF_EVEN));
    }

    public Optional<Semaforo> semaforo() {
        return global().map(Semaforo::porPuntaje);
    }
}
