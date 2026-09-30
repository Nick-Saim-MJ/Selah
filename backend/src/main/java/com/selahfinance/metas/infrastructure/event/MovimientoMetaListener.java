package com.selahfinance.metas.infrastructure.event;

import com.selahfinance.metas.application.port.in.ReevaluarMetaUseCase;
import com.selahfinance.movimientos.domain.event.MovimientoEliminado;
import com.selahfinance.movimientos.domain.event.MovimientoRegistrado;
import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Un aporte puede completar la meta (o, si se elimina, reabrirla). */
@Component
@RequiredArgsConstructor
class MovimientoMetaListener {

    private final ReevaluarMetaUseCase metas;

    @EventListener
    void alRegistrar(MovimientoRegistrado e) {
        if (e.tipoMovimiento() == TipoMovimiento.APORTE_META && e.metaId() != null) {
            metas.reevaluar(e.metaId());
        }
    }

    @EventListener
    void alEliminar(MovimientoEliminado e) {
        if (e.tipoMovimiento() == TipoMovimiento.APORTE_META && e.metaId() != null) {
            metas.reevaluar(e.metaId());
        }
    }
}
