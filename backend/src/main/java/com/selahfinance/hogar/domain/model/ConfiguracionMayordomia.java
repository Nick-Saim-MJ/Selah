package com.selahfinance.hogar.domain.model;

import com.selahfinance.shared.domain.DomainException;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Parámetros de mayordomía del hogar (wizard paso 3, editable en Configuración).
 * Porcentajes en base 100.
 */
public record ConfiguracionMayordomia(
        UUID hogarId,
        BigDecimal pctDiezmo,
        boolean ofrendaActiva,
        BigDecimal pctOfrenda,
        Integer diaEntregaDiezmo,
        boolean modoSabadoActivo) {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public ConfiguracionMayordomia {
        validarPorcentaje(pctDiezmo, "diezmo");
        validarPorcentaje(pctOfrenda, "ofrenda");
        if (pctDiezmo.add(pctOfrenda).compareTo(CIEN) > 0) {
            throw new DomainException("PORCENTAJES_EXCEDEN", "Diezmo + ofrenda no puede superar el 100%");
        }
        if (diaEntregaDiezmo != null && (diaEntregaDiezmo < 1 || diaEntregaDiezmo > 31)) {
            throw new DomainException("DIA_INVALIDO", "El día de entrega debe estar entre 1 y 31");
        }
    }

    /** 10% de diezmo (Mal. 3:10), ofrenda 2% y Modo Sábado activo, como en el prototipo. */
    public static ConfiguracionMayordomia porDefecto(UUID hogarId) {
        return new ConfiguracionMayordomia(hogarId, BigDecimal.TEN, true, BigDecimal.valueOf(2), null, true);
    }

    /** Porcentaje de ofrenda efectivo (0 si el hogar no aparta ofrenda). */
    public BigDecimal pctOfrendaEfectivo() {
        return ofrendaActiva ? pctOfrenda : BigDecimal.ZERO;
    }

    private static void validarPorcentaje(BigDecimal valor, String nombre) {
        if (valor == null || valor.signum() < 0 || valor.compareTo(CIEN) > 0) {
            throw new DomainException("PORCENTAJE_INVALIDO", "El porcentaje de " + nombre + " debe estar entre 0 y 100");
        }
    }
}
