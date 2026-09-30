package com.selahfinance.reportes.application.port.out;

import com.selahfinance.reportes.domain.model.LineaPresupuesto;
import com.selahfinance.reportes.domain.model.ResumenMes;
import com.selahfinance.shared.domain.Periodo;
import java.util.List;
import java.util.UUID;

/** Cifras financieras del hogar, leídas de la fuente única (movimientos) y sus vistas. */
public interface DatosFinancierosPort {

    ResumenMes mes(UUID hogarId, Periodo periodo);

    /** Todas las categorías de gasto activas (o con gasto en el mes), con su plan y lo real. */
    List<LineaPresupuesto> presupuestoVsReal(UUID hogarId, Periodo periodo);
}
