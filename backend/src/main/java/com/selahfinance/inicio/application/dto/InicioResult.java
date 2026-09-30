package com.selahfinance.inicio.application.dto;

import com.selahfinance.mayordomia.domain.model.ResumenMayordomia;
import com.selahfinance.metas.domain.model.MetaConProgreso;
import com.selahfinance.reflexion.domain.model.TarjetaReflexion;
import com.selahfinance.reportes.domain.model.ReporteFinanciero;
import java.util.Optional;

/** Todo lo que necesita la pantalla Inicio en una sola llamada (spec 4.3). */
public record InicioResult(
        ReporteFinanciero financiero,
        ResumenMayordomia mayordomia,
        Optional<MetaConProgreso> metaPrincipal,
        TarjetaReflexion reflexion,
        long notificacionesNoLeidas) {
}
