package com.selahfinance.deudas.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/** Reacciona a los movimientos PAGO_DEUDA de la fuente única. */
public interface AplicarPagoDeudaUseCase {

    /** Descuenta el capital pagado del saldo. Lanza si el pago excede lo que se debe (revierte el movimiento). */
    void aplicar(UUID hogarId, UUID movimientoId, UUID deudaId, BigDecimal monto);

    /** Devuelve el capital al saldo si el movimiento se elimina. No hace nada si nunca se aplicó. */
    void revertir(UUID movimientoId);
}
