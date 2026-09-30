package com.selahfinance.deudas.domain.model;

import com.selahfinance.shared.domain.Dinero;

/** Cómo se reparte un pago: lo que cubre intereses y lo que reduce el capital. */
public record DetallePago(Dinero interes, Dinero capital) {

    public Dinero total() {
        return interes.sumar(capital);
    }
}
