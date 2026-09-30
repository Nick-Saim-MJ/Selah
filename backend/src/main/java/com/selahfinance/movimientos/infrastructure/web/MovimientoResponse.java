package com.selahfinance.movimientos.infrastructure.web;

import com.selahfinance.movimientos.domain.model.Movimiento;
import com.selahfinance.movimientos.domain.model.OrigenMovimiento;
import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record MovimientoResponse(
        UUID id,
        TipoMovimiento tipo,
        BigDecimal monto,
        LocalDate fecha,
        String descripcion,
        String nota,
        UUID categoriaId,
        UUID fuenteIngresoId,
        UUID metaId,
        UUID deudaId,
        UUID destinoOfrendaId,
        boolean esPresupuestado,
        OrigenMovimiento origen,
        UUID registradoPor) {

    static MovimientoResponse de(Movimiento m) {
        var d = m.datos();
        return new MovimientoResponse(m.id(), d.tipo(), d.monto().valor(), d.fecha(), d.descripcion(), d.nota(),
                d.categoriaId(), d.fuenteIngresoId(), d.metaId(), d.deudaId(), d.destinoOfrendaId(),
                d.esPresupuestado(), d.origen(), d.registradoPor());
    }
}
