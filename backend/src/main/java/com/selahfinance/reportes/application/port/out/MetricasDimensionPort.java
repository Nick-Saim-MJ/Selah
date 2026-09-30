package com.selahfinance.reportes.application.port.out;

import com.selahfinance.reportes.domain.model.CumplimientoHabito;
import com.selahfinance.shared.domain.DimensionMayordomia;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Fuente de métricas de hábitos para una dimensión (Tiempo, Talento o Templo).
 *
 * <p>Puede haber VARIAS implementaciones activas a la vez y el caso de uso las combina:
 * <ul>
 *   <li>Local: registros hechos en la app (módulo habitos).</li>
 *   <li>Externa: API de otro equipo (integraciones.infrastructure.adapter.HttpMetricasDimensionAdapter).</li>
 * </ul>
 * Una fuente caída devuelve lista vacía: el reporte se genera igual con lo disponible.
 */
public interface MetricasDimensionPort {

    List<CumplimientoHabito> obtener(UUID usuarioId, DimensionMayordomia dimension, LocalDate desde, LocalDate hasta);
}
