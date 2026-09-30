package com.selahfinance.reportes.domain.model;

import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.Periodo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Reporte financiero mensual: ratios, semáforo y presupuestado vs. real.
 * El semáforo mide (gastos + pagos de deuda) sobre el ingreso; sin ingresos no hay veredicto.
 */
public record ReporteFinanciero(
        Periodo periodo,
        ResumenMes mes,
        BigDecimal ratioGasto,
        BigDecimal ratioDeuda,
        BigDecimal ratioAhorro,
        Optional<Semaforo> semaforo,
        List<LineaPresupuesto> lineas) {

    public static ReporteFinanciero armar(Periodo periodo, ResumenMes mes, List<LineaPresupuesto> lineas) {
        var i = mes.indicadores();
        if (!i.ingresos().esPositivo()) {
            return new ReporteFinanciero(periodo, mes, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    Optional.empty(), lineas);
        }
        return new ReporteFinanciero(periodo, mes, pct(i.gastos(), i.ingresos()), pct(i.pagosDeuda(), i.ingresos()),
                pct(i.aportesMeta(), i.ingresos()),
                Optional.of(Semaforo.porRatioGasto(pct(i.gastos().sumar(i.pagosDeuda()), i.ingresos()))), lineas);
    }

    /** Categoría que más se pasó de su presupuesto (la que "empuja" el color del semáforo). */
    public Optional<LineaPresupuesto> principalDesvio() {
        return lineas.stream()
                .filter(LineaPresupuesto::excedido)
                .max(Comparator.comparing(LineaPresupuesto::variacionPct));
    }

    private static BigDecimal pct(Dinero parte, Dinero total) {
        return parte.valor().multiply(BigDecimal.valueOf(100)).divide(total.valor(), 1, RoundingMode.HALF_EVEN);
    }
}
