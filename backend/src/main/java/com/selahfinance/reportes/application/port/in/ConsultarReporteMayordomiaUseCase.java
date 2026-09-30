package com.selahfinance.reportes.application.port.in;

import com.selahfinance.reportes.domain.model.ReporteMayordomia;
import com.selahfinance.shared.domain.Periodo;
import java.util.Optional;
import java.util.UUID;

public interface ConsultarReporteMayordomiaUseCase {

    /** La última foto guardada (sin recalcular). Vacío si aún no se generó. */
    Optional<ReporteMayordomia> existente(UUID usuarioId, UUID hogarId, Periodo periodo);
}
