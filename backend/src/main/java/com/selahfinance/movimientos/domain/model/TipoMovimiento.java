package com.selahfinance.movimientos.domain.model;

public enum TipoMovimiento {
    INGRESO,
    GASTO,
    /** Entrega efectiva del diezmo apartado (se registra desde Diezmos, no desde el formulario general). */
    DIEZMO,
    OFRENDA,
    PAGO_DEUDA,
    APORTE_META;

    public boolean esEntrada() {
        return this == INGRESO;
    }
}
