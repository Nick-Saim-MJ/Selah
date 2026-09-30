package com.selahfinance.movimientos.infrastructure.web;

import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record RegistrarMovimientoRequest(
        @NotNull TipoMovimiento tipo,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal monto,
        @NotNull LocalDate fecha,
        @Size(max = 160) String descripcion,
        @Size(max = 2000) String nota,
        UUID categoriaId,
        UUID fuenteIngresoId,
        UUID metaId,
        UUID deudaId,
        UUID destinoOfrendaId,
        Boolean esPresupuestado) {
}
