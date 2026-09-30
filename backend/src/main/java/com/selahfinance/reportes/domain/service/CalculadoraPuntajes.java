package com.selahfinance.reportes.domain.service;

import com.selahfinance.reportes.domain.model.CumplimientoHabito;
import com.selahfinance.reportes.domain.model.IndicadoresTesoro;
import com.selahfinance.reportes.domain.model.Semaforo;
import com.selahfinance.shared.domain.Dinero;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Algoritmo v1.0 del reporte de mayordomía integral (documentado en docs/reporte-4T.md).
 * Cambiar pesos o umbrales implica subir la versión guardada en reporte_mayordomia.version_algoritmo.
 */
public final class CalculadoraPuntajes {

    public static final String VERSION = "1.0";

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal PESO_FIDELIDAD = new BigDecimal("0.40");
    private static final BigDecimal PESO_SEMAFORO = new BigDecimal("0.25");
    private static final BigDecimal PESO_AHORRO = new BigDecimal("0.20");
    private static final BigDecimal PESO_DEUDA = new BigDecimal("0.15");
    private static final BigDecimal META_AHORRO_PCT = BigDecimal.TEN;
    private static final BigDecimal LIMITE_DEUDA_PCT = BigDecimal.valueOf(40);

    /**
     * Tesoro = 40% fidelidad en el diezmo + 25% semáforo de gasto + 20% ahorro (meta 10%)
     * + 15% salud de deuda (cuotas ≤ 40% del ingreso). {@code null} si no hubo ingresos.
     */
    public BigDecimal puntajeTesoro(IndicadoresTesoro i) {
        if (!i.ingresos().esPositivo()) {
            return null;
        }
        BigDecimal fidelidad = i.diezmoApartado().esPositivo()
                ? min100(pct(i.diezmoEntregado(), i.diezmoApartado()))
                : CIEN;
        BigDecimal semaforo = switch (Semaforo.porRatioGasto(pct(i.gastos().sumar(i.pagosDeuda()), i.ingresos()))) {
            case VERDE -> CIEN;
            case AMARILLO -> BigDecimal.valueOf(60);
            case ROJO -> BigDecimal.valueOf(20);
        };
        BigDecimal ahorro = min100(pct(i.aportesMeta(), i.ingresos()).multiply(CIEN)
                .divide(META_AHORRO_PCT, 2, RoundingMode.HALF_EVEN));
        BigDecimal ratioDeuda = pct(i.cuotasMensualesDeuda(), i.ingresos());
        BigDecimal deuda = ratioDeuda.compareTo(LIMITE_DEUDA_PCT) <= 0
                ? CIEN
                : CIEN.subtract(ratioDeuda.subtract(LIMITE_DEUDA_PCT).multiply(BigDecimal.valueOf(5))).max(BigDecimal.ZERO);

        return fidelidad.multiply(PESO_FIDELIDAD)
                .add(semaforo.multiply(PESO_SEMAFORO))
                .add(ahorro.multiply(PESO_AHORRO))
                .add(deuda.multiply(PESO_DEUDA))
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    /** Tiempo, Talento y Templo: promedio del % de cumplimiento de cada hábito (tope 100% por hábito). */
    public BigDecimal puntajeHabitos(List<CumplimientoHabito> habitos) {
        var validos = habitos.stream().filter(h -> h.metaPeriodo().signum() > 0).toList();
        if (validos.isEmpty()) {
            return null;
        }
        BigDecimal suma = validos.stream()
                .map(h -> min100(h.valorRegistrado().multiply(CIEN).divide(h.metaPeriodo(), 2, RoundingMode.HALF_EVEN)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(validos.size()), 2, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal pct(Dinero parte, Dinero total) {
        return parte.valor().multiply(CIEN).divide(total.valor(), 2, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal min100(BigDecimal valor) {
        return valor.min(CIEN);
    }
}
