package com.selahfinance.reportes.domain.model;

import java.math.BigDecimal;

/** Resumen visual único de salud (verde / amarillo / rojo). */
public enum Semaforo {
    VERDE,
    AMARILLO,
    ROJO;

    private static final BigDecimal LIMITE_VERDE_GASTO = BigDecimal.valueOf(70);
    private static final BigDecimal LIMITE_AMARILLO_GASTO = BigDecimal.valueOf(90);
    private static final BigDecimal LIMITE_VERDE_PUNTAJE = BigDecimal.valueOf(75);
    private static final BigDecimal LIMITE_AMARILLO_PUNTAJE = BigDecimal.valueOf(50);

    /**
     * Semáforo financiero (modelo SBS simplificado) según % de (gastos + pagos de deuda) sobre el ingreso:
     * verde &lt; 70%, amarillo 70–90%, rojo &gt; 90%.
     */
    public static Semaforo porRatioGasto(BigDecimal porcentajeGastoSobreIngreso) {
        if (porcentajeGastoSobreIngreso.compareTo(LIMITE_VERDE_GASTO) < 0) {
            return VERDE;
        }
        return porcentajeGastoSobreIngreso.compareTo(LIMITE_AMARILLO_GASTO) <= 0 ? AMARILLO : ROJO;
    }

    /** Semáforo del reporte integral 4T según el puntaje global (0–100). */
    public static Semaforo porPuntaje(BigDecimal puntaje) {
        if (puntaje.compareTo(LIMITE_VERDE_PUNTAJE) >= 0) {
            return VERDE;
        }
        return puntaje.compareTo(LIMITE_AMARILLO_PUNTAJE) >= 0 ? AMARILLO : ROJO;
    }
}
