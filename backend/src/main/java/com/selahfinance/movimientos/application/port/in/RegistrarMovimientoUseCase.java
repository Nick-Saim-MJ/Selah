package com.selahfinance.movimientos.application.port.in;

import com.selahfinance.movimientos.domain.model.Movimiento;
import com.selahfinance.movimientos.domain.model.OrigenMovimiento;
import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface RegistrarMovimientoUseCase {

    /**
     * Registra un movimiento. Si {@code origen != APP} y ya existe uno con la misma
     * {@code referenciaExterna}, devuelve el existente (idempotente para integraciones).
     */
    Movimiento registrar(Comando comando);

    record Comando(
            UUID hogarId,
            UUID usuarioId,
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
            Boolean esPresupuestado,
            OrigenMovimiento origen,
            String referenciaExterna) {
    }
}
