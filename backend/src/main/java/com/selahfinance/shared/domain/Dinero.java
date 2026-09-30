package com.selahfinance.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Monto monetario inmutable con 2 decimales y redondeo bancario (HALF_EVEN).
 * Nunca usar double/float para dinero.
 */
public record Dinero(BigDecimal valor) implements Comparable<Dinero> {

    public static final Dinero CERO = new Dinero(BigDecimal.ZERO);
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public Dinero {
        Objects.requireNonNull(valor, "valor");
        valor = valor.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Dinero de(BigDecimal valor) {
        return new Dinero(valor);
    }

    public static Dinero de(String valor) {
        return new Dinero(new BigDecimal(valor));
    }

    public Dinero sumar(Dinero otro) {
        return new Dinero(valor.add(otro.valor));
    }

    public Dinero restar(Dinero otro) {
        return new Dinero(valor.subtract(otro.valor));
    }

    /** Aplica un porcentaje expresado en base 100 (10 = 10%). */
    public Dinero porcentaje(BigDecimal porcentaje) {
        return new Dinero(valor.multiply(porcentaje).divide(CIEN, 2, RoundingMode.HALF_EVEN));
    }

    public boolean esPositivo() {
        return valor.signum() > 0;
    }

    public boolean esNegativo() {
        return valor.signum() < 0;
    }

    @Override
    public int compareTo(Dinero otro) {
        return valor.compareTo(otro.valor);
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }
}
