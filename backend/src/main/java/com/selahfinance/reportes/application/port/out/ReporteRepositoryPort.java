package com.selahfinance.reportes.application.port.out;

import com.selahfinance.reportes.domain.model.ReporteMayordomia;
import com.selahfinance.shared.domain.Periodo;
import java.util.Optional;
import java.util.UUID;

public interface ReporteRepositoryPort {

    /** Crea o reemplaza la foto del (hogar, usuario, mes). */
    void guardar(ReporteMayordomia reporte);

    Optional<ReporteMayordomia> porUsuarioYPeriodo(UUID usuarioId, UUID hogarId, Periodo periodo);
}
