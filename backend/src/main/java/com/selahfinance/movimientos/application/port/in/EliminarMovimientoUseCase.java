package com.selahfinance.movimientos.application.port.in;

import java.util.UUID;

public interface EliminarMovimientoUseCase {

    void eliminar(UUID hogarId, UUID movimientoId);
}
