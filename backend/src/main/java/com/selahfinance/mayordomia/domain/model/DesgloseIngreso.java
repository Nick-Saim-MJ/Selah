package com.selahfinance.mayordomia.domain.model;

import com.selahfinance.shared.domain.Dinero;
import java.math.BigDecimal;

/**
 * Lo que el usuario ve ANTES de confirmar un ingreso:
 * "de estos S/ 1000, S/ 100 son diezmo y S/ 20 ofrenda; quedan S/ 880".
 */
public record DesgloseIngreso(
        Dinero ingreso,
        BigDecimal pctDiezmo,
        Dinero diezmo,
        BigDecimal pctOfrenda,
        Dinero ofrenda,
        Dinero disponible) {
}
