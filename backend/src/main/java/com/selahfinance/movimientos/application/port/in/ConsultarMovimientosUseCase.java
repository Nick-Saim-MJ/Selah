package com.selahfinance.movimientos.application.port.in;

import com.selahfinance.movimientos.domain.model.Movimiento;
import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import com.selahfinance.shared.application.Pagina;
import java.time.LocalDate;
import java.util.UUID;

public interface ConsultarMovimientosUseCase {

    Pagina<Movimiento> buscar(Filtro filtro);

    Movimiento obtener(UUID hogarId, UUID movimientoId);

    record Filtro(
            UUID hogarId,
            TipoMovimiento tipo,
            LocalDate desde,
            LocalDate hasta,
            UUID categoriaId,
            String texto,
            int pagina,
            int tamanio) {
    }
}
