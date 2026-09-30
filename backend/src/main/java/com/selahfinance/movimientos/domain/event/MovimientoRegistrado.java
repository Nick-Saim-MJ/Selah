package com.selahfinance.movimientos.domain.event;

import com.selahfinance.movimientos.domain.model.OrigenMovimiento;
import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import com.selahfinance.shared.domain.event.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Publicado para integraciones como {@code movimiento.registrado}. Lleva las referencias
 * (categoría, meta, deuda) para que otros módulos reaccionen sin consultar movimientos.
 */
public record MovimientoRegistrado(
        UUID movimientoId,
        UUID hogarId,
        UUID registradoPor,
        TipoMovimiento tipoMovimiento,
        BigDecimal monto,
        LocalDate fecha,
        UUID categoriaId,
        UUID metaId,
        UUID deudaId,
        OrigenMovimiento origen,
        Instant ocurridoEn) implements DomainEvent {

    @Override
    public String tipo() {
        return "movimiento.registrado";
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
