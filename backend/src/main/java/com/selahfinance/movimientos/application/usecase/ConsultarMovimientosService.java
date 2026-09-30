package com.selahfinance.movimientos.application.usecase;

import com.selahfinance.movimientos.application.port.in.ConsultarMovimientosUseCase;
import com.selahfinance.movimientos.application.port.out.MovimientoRepositoryPort;
import com.selahfinance.movimientos.domain.model.Movimiento;
import com.selahfinance.shared.application.Pagina;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ConsultarMovimientosService implements ConsultarMovimientosUseCase {

    private static final int TAMANIO_MAXIMO = 100;

    private final MovimientoRepositoryPort movimientos;

    @Override
    public Pagina<Movimiento> buscar(Filtro f) {
        int tamanio = Math.min(Math.max(f.tamanio(), 1), TAMANIO_MAXIMO);
        return movimientos.buscar(new Filtro(f.hogarId(), f.tipo(), f.desde(), f.hasta(), f.categoriaId(), f.texto(),
                Math.max(f.pagina(), 0), tamanio));
    }

    @Override
    public Movimiento obtener(UUID hogarId, UUID movimientoId) {
        return movimientos.porId(hogarId, movimientoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Movimiento", movimientoId));
    }
}
