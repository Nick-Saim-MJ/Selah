package com.selahfinance.movimientos.domain.event;

import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import com.selahfinance.shared.domain.event.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Publicado para integraciones como {@code movimiento.eliminado}. */
public record MovimientoEliminado(
        UUID movimientoId,
        UUID hogarId,
        TipoMovimiento tipoMovimiento,
        BigDecimal monto,
        UUID metaId,
        UUID deudaId,
        Instant ocurridoEn) implements DomainEvent {

    @Override
    public String tipo() {
        return "movimiento.eliminado";
    }

    @Override
    public String agregado() {
        return "Movimiento";
    }

    @Override
    public UUID agregadoId() {
        return movimientoId;
    }
}
