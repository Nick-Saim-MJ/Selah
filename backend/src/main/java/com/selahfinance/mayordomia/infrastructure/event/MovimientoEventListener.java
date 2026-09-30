package com.selahfinance.mayordomia.infrastructure.event;

import com.selahfinance.mayordomia.application.port.in.GestionarApartadosUseCase;
import com.selahfinance.movimientos.domain.event.MovimientoEliminado;
import com.selahfinance.movimientos.domain.event.MovimientoRegistrado;
import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Escucha la fuente única (movimientos). Es síncrono: corre dentro de la transacción del
 * registro, así un ingreso nunca queda guardado sin su diezmo apartado.
 */
@Component
@RequiredArgsConstructor
class MovimientoEventListener {

    private final GestionarApartadosUseCase apartados;

    @EventListener
    void alRegistrar(MovimientoRegistrado evento) {
        if (evento.tipoMovimiento() == TipoMovimiento.INGRESO) {
            apartados.generarParaIngreso(evento.hogarId(), evento.movimientoId(), evento.monto(), evento.fecha());
        }
    }

    @EventListener
    void alEliminar(MovimientoEliminado evento) {
        if (evento.tipoMovimiento() == TipoMovimiento.INGRESO) {
            apartados.anularParaIngreso(evento.movimientoId());
        }
    }
}
