package com.selahfinance.reportes.domain.model;

import com.selahfinance.shared.domain.Dinero;

/** Cifras del mes para un hogar: indicadores + saldo disponible (con diezmo/ofrenda ya apartados). */
public record ResumenMes(IndicadoresTesoro indicadores, Dinero saldoDisponible, Dinero apartadoTotal,
        Dinero apartadoPendiente) {
}
