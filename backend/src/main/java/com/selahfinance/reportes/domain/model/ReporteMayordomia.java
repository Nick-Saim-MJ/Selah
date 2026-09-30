package com.selahfinance.reportes.domain.model;

import com.selahfinance.shared.domain.DimensionMayordomia;
import com.selahfinance.shared.domain.Periodo;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Reporte integral de mayordomía (Tiempo, Talento, Tesoro, Templo) de una persona en un mes.
 * Es una foto: se guarda con la versión del algoritmo que la calculó.
 *
 * @param diezmoAlDia null si en el periodo no hubo diezmo que entregar; nunca contiene montos
 */
public record ReporteMayordomia(
        UUID usuarioId,
        UUID hogarId,
        Periodo periodo,
        PuntajesMayordomia puntajes,
        Map<DimensionMayordomia, List<CumplimientoHabito>> habitos,
        Boolean diezmoAlDia,
        Instant generadoEn,
        String versionAlgoritmo) {

    public Optional<Semaforo> semaforo() {
        return puntajes.semaforo();
    }
}
