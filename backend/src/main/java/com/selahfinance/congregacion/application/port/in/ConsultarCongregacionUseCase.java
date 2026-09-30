package com.selahfinance.congregacion.application.port.in;

import com.selahfinance.congregacion.domain.model.ReporteCompartido;
import com.selahfinance.congregacion.domain.model.ResumenCongregacion;
import com.selahfinance.shared.domain.Periodo;
import java.util.List;
import java.util.UUID;

/** Vista del pastor sobre SU iglesia. {@code pastorId} se verifica contra la base de datos. */
public interface ConsultarCongregacionUseCase {

    /** Totales anónimos de la congregación. */
    ResumenCongregacion resumen(UUID pastorId, Periodo periodo);

    /**
     * Reportes de los hermanos que decidieron compartir el suyo (se recalculan al pedirlos).
     * Quien retiró su consentimiento deja de aparecer de inmediato.
     */
    List<ReporteCompartido> reportesCompartidos(UUID pastorId, Periodo periodo);
}
