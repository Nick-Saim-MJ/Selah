package com.selahfinance.metas.domain.model;

import com.selahfinance.shared.domain.Dinero;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Meta junto con lo aportado hasta hoy (suma de movimientos APORTE_META). */
public record MetaConProgreso(MetaAhorro meta, Dinero montoActual) {

    public BigDecimal porcentaje() {
        BigDecimal pct = montoActual.valor().multiply(BigDecimal.valueOf(100))
                .divide(meta.montoObjetivo().valor(), 2, RoundingMode.HALF_EVEN);
        return pct.min(BigDecimal.valueOf(100));
    }

    public Dinero restante() {
        Dinero resto = meta.montoObjetivo().restar(montoActual);
        return resto.esNegativo() ? Dinero.CERO : resto;
    }

    public boolean alcanzada() {
        return montoActual.compareTo(meta.montoObjetivo()) >= 0;
    }

    /** Cuánto aportar por mes para llegar a la fecha objetivo (null si no hay fecha o ya venció/se alcanzó). */
    public Dinero aporteMensualSugerido(LocalDate hoy) {
        if (meta.fechaObjetivo() == null || alcanzada()) {
            return null;
        }
        long meses = ChronoUnit.MONTHS.between(hoy.withDayOfMonth(1), meta.fechaObjetivo().withDayOfMonth(1));
        if (meses <= 0) {
            return restante();
        }
        return Dinero.de(restante().valor().divide(BigDecimal.valueOf(meses), 2, RoundingMode.CEILING));
    }
}
