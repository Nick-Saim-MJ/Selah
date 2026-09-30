package com.selahfinance.reportes.domain.model;

import com.selahfinance.shared.domain.Dinero;

/** Agregados financieros del periodo (provienen de v_resumen_mensual y deudas activas). */
public record IndicadoresTesoro(
        Dinero ingresos,
        Dinero gastos,
        Dinero pagosDeuda,
        Dinero aportesMeta,
        Dinero diezmoApartado,
        Dinero diezmoEntregado,
        Dinero cuotasMensualesDeuda) {
}
