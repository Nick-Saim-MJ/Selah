package com.selahfinance.reportes.domain.model;

import com.selahfinance.shared.domain.Dinero;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/** Presupuestado vs. real de una categoría de gasto en un mes (spec 4.7). */
public record LineaPresupuesto(UUID categoriaId, String nombre, String color, Dinero planeado, Dinero real) {

    /** (real − planeado) / planeado en %; null si no hay presupuesto asignado. */
    public BigDecimal variacionPct() {
        if (!planeado.esPositivo()) {
            return null;
        }
        return real.restar(planeado).valor().multiply(BigDecimal.valueOf(100))
                .divide(planeado.valor(), 1, RoundingMode.HALF_EVEN);
    }

    public boolean excedido() {
        return planeado.esPositivo() && real.compareTo(planeado) > 0;
    }
}
