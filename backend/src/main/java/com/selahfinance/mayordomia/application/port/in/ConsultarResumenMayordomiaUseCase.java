package com.selahfinance.mayordomia.application.port.in;

import com.selahfinance.mayordomia.domain.model.ResumenMayordomia;
import com.selahfinance.shared.domain.Periodo;
import java.util.List;
import java.util.UUID;

public interface ConsultarResumenMayordomiaUseCase {

    ResumenMayordomia resumen(UUID hogarId, Periodo periodo);

    /**
     * Fidelidad mes a mes: los {@code meses} periodos que terminan en {@code hasta}, del más antiguo al más
     * reciente. Sirve para ver la constancia a lo largo del año, no solo el mes actual (spec 4.5).
     */
    List<ResumenMayordomia> historial(UUID hogarId, Periodo hasta, int meses);
}
