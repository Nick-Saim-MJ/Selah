package com.selahfinance.movimientos.domain.model;

import com.selahfinance.shared.domain.Dinero;
import java.time.LocalDate;
import java.util.UUID;

/** Datos capturados de un movimiento. Las invariantes se validan en {@link Movimiento#registrar}. */
public record DatosMovimiento(
        UUID hogarId,
        UUID registradoPor,
        TipoMovimiento tipo,
        Dinero monto,
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
        String referenciaExterna) {
}
