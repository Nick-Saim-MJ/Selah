package com.selahfinance.habitos.domain.model;

import com.selahfinance.shared.domain.DimensionMayordomia;
import com.selahfinance.shared.domain.DomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Hábito medible de una dimensión de la mayordomía (catálogo del sistema o del hogar). */
public record Habito(
        UUID id,
        String codigo,
        DimensionMayordomia dimension,
        String nombre,
        String descripcion,
        UnidadHabito unidad,
        FrecuenciaMeta frecuenciaMeta,
        BigDecimal metaValor,
        String referenciaBiblica,
        int orden) {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal SIETE = BigDecimal.valueOf(7);

    /** Valida un valor registrado para este hábito. */
    public void validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() < 0) {
            throw new DomainException("VALOR_INVALIDO", "El valor no puede ser negativo");
        }
        if (unidad == UnidadHabito.BOOLEANO && valor.compareTo(BigDecimal.ZERO) != 0 && valor.compareTo(BigDecimal.ONE) != 0) {
            throw new DomainException("VALOR_INVALIDO", "«" + nombre + "» solo admite cumplido (1) o no cumplido (0)");
        }
    }

    /** Meta acumulada para un rango de fechas (ambas incluidas): diaria × días, o semanal × días/7. */
    public BigDecimal metaParaRango(LocalDate desde, LocalDate hasta) {
        long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;
        if (dias <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal porDia = frecuenciaMeta == FrecuenciaMeta.DIARIA ? metaValor : metaValor.divide(SIETE, 6, RoundingMode.HALF_EVEN);
        return porDia.multiply(BigDecimal.valueOf(dias)).setScale(2, RoundingMode.HALF_EVEN);
    }

    /** Porcentaje de cumplimiento (tope 100) de {@code valor} frente a {@code meta}. */
    public static BigDecimal cumplimientoPct(BigDecimal valor, BigDecimal meta) {
        if (meta.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return valor.multiply(CIEN).divide(meta, 2, RoundingMode.HALF_EVEN).min(CIEN);
    }
}
