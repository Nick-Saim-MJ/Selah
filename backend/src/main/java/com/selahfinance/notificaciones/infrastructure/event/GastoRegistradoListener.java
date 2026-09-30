package com.selahfinance.notificaciones.infrastructure.event;

import com.selahfinance.movimientos.domain.event.MovimientoRegistrado;
import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import com.selahfinance.notificaciones.application.port.in.GenerarAvisosUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Cada gasto puede hacer que una categoría supere su presupuesto. */
@Component
@RequiredArgsConstructor
class GastoRegistradoListener {

    private final GenerarAvisosUseCase avisos;

    @EventListener
    void alRegistrar(MovimientoRegistrado e) {
        if (e.tipoMovimiento() == TipoMovimiento.GASTO && e.categoriaId() != null) {
            avisos.alertarSiPresupuestoExcedido(e.hogarId(), e.registradoPor(), e.categoriaId(), e.monto(), e.fecha());
        }
    }
}
