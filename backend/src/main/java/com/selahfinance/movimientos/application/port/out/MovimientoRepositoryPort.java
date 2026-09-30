package com.selahfinance.movimientos.application.port.out;

import com.selahfinance.movimientos.application.port.in.ConsultarMovimientosUseCase.Filtro;
import com.selahfinance.movimientos.domain.model.Movimiento;
import com.selahfinance.movimientos.domain.model.OrigenMovimiento;
import com.selahfinance.shared.application.Pagina;
import java.util.Optional;
import java.util.UUID;

public interface MovimientoRepositoryPort {

    Movimiento guardar(Movimiento movimiento);

    /** Solo movimientos no eliminados del hogar indicado. */
    Optional<Movimiento> porId(UUID hogarId, UUID movimientoId);

    Optional<Movimiento> porReferenciaExterna(UUID hogarId, OrigenMovimiento origen, String referenciaExterna);

    Pagina<Movimiento> buscar(Filtro filtro);
}
