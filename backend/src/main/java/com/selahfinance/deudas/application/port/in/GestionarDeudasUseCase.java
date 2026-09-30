package com.selahfinance.deudas.application.port.in;

import com.selahfinance.deudas.domain.model.Deuda;
import com.selahfinance.deudas.domain.model.ResumenDeudas;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface GestionarDeudasUseCase {

    /** Deudas activas, cuota total y alerta de sobreendeudamiento. */
    ResumenDeudas resumen(UUID hogarId);

    Deuda crear(UUID hogarId, ComandoCrear comando);

    Deuda actualizar(UUID hogarId, UUID deudaId, String nombre, String acreedor, Integer diaPago);

    record ComandoCrear(
            String nombre,
            String acreedor,
            BigDecimal montoOriginal,
            BigDecimal saldoActual,
            BigDecimal tasaAnual,
            int plazoMeses,
            LocalDate fechaInicio,
            Integer diaPago) {
    }
}
