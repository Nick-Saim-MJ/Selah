package com.selahfinance.mayordomia.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Reacciona a los ingresos registrados/eliminados en el módulo movimientos. */
public interface GestionarApartadosUseCase {

    void generarParaIngreso(UUID hogarId, UUID movimientoIngresoId, BigDecimal monto, LocalDate fecha);

    void anularParaIngreso(UUID movimientoIngresoId);
}
