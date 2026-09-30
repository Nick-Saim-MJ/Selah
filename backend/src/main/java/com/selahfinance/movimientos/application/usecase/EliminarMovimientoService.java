package com.selahfinance.movimientos.application.usecase;

import com.selahfinance.movimientos.application.port.in.EliminarMovimientoUseCase;
import com.selahfinance.movimientos.application.port.out.MovimientoRepositoryPort;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import com.selahfinance.shared.application.port.out.EventPublisherPort;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class EliminarMovimientoService implements EliminarMovimientoUseCase {

    private final MovimientoRepositoryPort movimientos;
    private final EventPublisherPort eventos;
    private final Clock clock;

    @Override
    @Transactional
    public void eliminar(UUID hogarId, UUID movimientoId) {
        var movimiento = movimientos.porId(hogarId, movimientoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Movimiento", movimientoId));
        var evento = movimiento.eliminar(clock.instant());
        movimientos.guardar(movimiento);
        eventos.publicar(evento);
    }
}
