package com.selahfinance.reportes.domain.model;

import java.math.BigDecimal;

/**
 * Meta vs. valor registrado de un hábito en el periodo. Puede venir de la app (fuente "APP") o del
 * sistema de otro equipo (fuente = código del proveedor externo).
 */
public record CumplimientoHabito(String codigo, String nombre, BigDecimal metaPeriodo, BigDecimal valorRegistrado,
        String fuente) {
}
