package com.selahfinance.congregacion.domain.service;

import com.selahfinance.congregacion.domain.model.ResumenCongregacion;
import com.selahfinance.reportes.domain.model.ReporteMayordomia;
import com.selahfinance.shared.domain.Periodo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;
import java.util.Objects;

/** Agrega reportes individuales en totales anónimos, aplicando el umbral mínimo de anonimato. */
public final class CalculadoraCongregacion {

    private CalculadoraCongregacion() {
    }

    public static ResumenCongregacion resumir(Periodo periodo, int miembrosActivos, List<ReporteMayordomia> reportes,
            int minimoAnonimato) {
        var conDatos = reportes.stream().filter(r -> r.puntajes().global().isPresent()).toList();
        boolean suficientes = conDatos.size() >= minimoAnonimato;
        if (!suficientes) {
            return new ResumenCongregacion(periodo, miembrosActivos, conDatos.size(), minimoAnonimato, false, null,
                    null, null, null, null, null);
        }
        return new ResumenCongregacion(periodo, miembrosActivos, conDatos.size(), minimoAnonimato, true,
                fidelidad(conDatos, minimoAnonimato),
                promedio(conDatos, r -> r.puntajes().tiempo(), minimoAnonimato),
                promedio(conDatos, r -> r.puntajes().talento(), minimoAnonimato),
                promedio(conDatos, r -> r.puntajes().tesoro(), minimoAnonimato),
                promedio(conDatos, r -> r.puntajes().templo(), minimoAnonimato),
                promedio(conDatos, r -> r.puntajes().global().orElse(null), minimoAnonimato));
    }

    /** Promedio de una dimensión, solo si al menos {@code minimo} personas tienen dato en ella. */
    private static BigDecimal promedio(List<ReporteMayordomia> reportes, Function<ReporteMayordomia, BigDecimal> dimension,
            int minimo) {
        var valores = reportes.stream().map(dimension).filter(Objects::nonNull).toList();
        if (valores.size() < minimo) {
            return null;
        }
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(valores.size()), 1, RoundingMode.HALF_EVEN);
    }

    /** % de hermanos con diezmo que ya lo entregó; requiere al menos {@code minimo} con diezmo del mes. */
    private static BigDecimal fidelidad(List<ReporteMayordomia> reportes, int minimo) {
        var conDiezmo = reportes.stream().map(ReporteMayordomia::diezmoAlDia).filter(Objects::nonNull).toList();
        if (conDiezmo.size() < minimo) {
            return null;
        }
        long alDia = conDiezmo.stream().filter(Boolean::booleanValue).count();
        return BigDecimal.valueOf(alDia * 100).divide(BigDecimal.valueOf(conDiezmo.size()), 1, RoundingMode.HALF_EVEN);
    }
}
