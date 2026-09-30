package com.selahfinance.mayordomia.domain.model;

public enum EstadoApartado {
    /** Calculado al registrar el ingreso; ya se descuenta del saldo disponible. */
    PENDIENTE,
    /** El usuario confirmó que lo entregó (existe un movimiento DIEZMO/OFRENDA). */
    ENTREGADO,
    /** El ingreso que lo originó fue eliminado. */
    ANULADO
}
