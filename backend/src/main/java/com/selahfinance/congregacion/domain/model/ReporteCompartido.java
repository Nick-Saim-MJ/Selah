package com.selahfinance.congregacion.domain.model;

import com.selahfinance.reportes.domain.model.ReporteMayordomia;
import java.util.UUID;

/**
 * Reporte 4T de un hermano que decidió compartirlo con su pastor: nombre, puntajes, semáforo,
 * desglose de hábitos y si su diezmo está al día. Nunca montos ni movimientos.
 */
public record ReporteCompartido(UUID usuarioId, String nombre, ReporteMayordomia reporte) {
}
