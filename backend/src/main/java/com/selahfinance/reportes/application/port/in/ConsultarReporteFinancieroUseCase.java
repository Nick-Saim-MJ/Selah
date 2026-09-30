package com.selahfinance.reportes.application.port.in;

import com.selahfinance.reportes.domain.model.ReporteFinanciero;
import com.selahfinance.shared.domain.Periodo;
import java.util.UUID;

public interface ConsultarReporteFinancieroUseCase {

    ReporteFinanciero generar(UUID hogarId, Periodo periodo);
}
