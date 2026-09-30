package com.selahfinance.reportes.application.port.out;

import java.util.List;
import java.util.UUID;

/** Cuentas activas con hogar (hermanos y pastores) para las que se genera el reporte nocturno. */
public interface CuentasParaReportePort {

    List<Cuenta> activas();

    record Cuenta(UUID usuarioId, UUID hogarId) {
    }
}
