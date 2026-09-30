package com.selahfinance.deudas.infrastructure.event;

import com.selahfinance.deudas.application.port.in.AplicarPagoDeudaUseCase;
import com.selahfinance.movimientos.domain.event.MovimientoEliminado;
import com.selahfinance.movimientos.domain.event.MovimientoRegistrado;
import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Síncrono y dentro de la transacción del movimiento: si el pago excede la deuda,
 * el caso de uso lanza y el movimiento tampoco se guarda.
 */
@Component
@RequiredArgsConstructor
class MovimientoDeudaListener {

    private final AplicarPagoDeudaUseCase deudas;

    @EventListener
    void alRegistrar(MovimientoRegistrado e) {
        if (e.tipoMovimiento() == TipoMovimiento.PAGO_DEUDA && e.deudaId() != null) {
            deudas.aplicar(e.hogarId(), e.movimientoId(), e.deudaId(), e.monto());
        }
    }

    @EventListener
    void alEliminar(MovimientoEliminado e) {
        if (e.tipoMovimiento() == TipoMovimiento.PAGO_DEUDA) {
            deudas.revertir(e.movimientoId());
        }
    }
}
