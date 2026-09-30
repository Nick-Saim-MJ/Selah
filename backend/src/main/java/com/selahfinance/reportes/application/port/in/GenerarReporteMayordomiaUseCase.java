package com.selahfinance.reportes.application.port.in;

import com.selahfinance.reportes.domain.model.ReporteMayordomia;
import com.selahfinance.shared.domain.Periodo;
import java.util.UUID;

public interface GenerarReporteMayordomiaUseCase {

    /** Calcula el reporte 4T del mes con los datos de hoy (mes en curso: hasta hoy) y guarda la foto. */
    ReporteMayordomia generar(UUID usuarioId, UUID hogarId, Periodo periodo);

    /** Genera el reporte de todas las cuentas con hogar (tarea nocturna). Devuelve cuántos generó. */
    int generarParaTodos(Periodo periodo);
}
