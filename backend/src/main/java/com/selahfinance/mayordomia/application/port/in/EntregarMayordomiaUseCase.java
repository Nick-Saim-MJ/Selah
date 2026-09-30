package com.selahfinance.mayordomia.application.port.in;

import com.selahfinance.mayordomia.domain.model.TipoApartado;
import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.Periodo;
import java.time.LocalDate;
import java.util.UUID;

/**
 * "Marcar como entregado": registra UN movimiento DIEZMO/OFRENDA por todo lo pendiente
 * del periodo y marca esos apartados como entregados.
 */
public interface EntregarMayordomiaUseCase {

    Resultado entregar(Comando comando);

    record Comando(UUID hogarId, UUID usuarioId, TipoApartado tipo, Periodo periodo, LocalDate fechaEntrega) {
    }

    record Resultado(UUID movimientoId, Dinero montoEntregado, int apartadosEntregados) {
    }
}
