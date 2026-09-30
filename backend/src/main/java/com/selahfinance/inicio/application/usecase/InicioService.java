package com.selahfinance.inicio.application.usecase;

import com.selahfinance.inicio.application.dto.InicioResult;
import com.selahfinance.inicio.application.port.in.ConsultarInicioUseCase;
import com.selahfinance.mayordomia.application.port.in.ConsultarResumenMayordomiaUseCase;
import com.selahfinance.metas.application.port.in.GestionarMetasUseCase;
import com.selahfinance.notificaciones.application.port.in.ConsultarBandejaUseCase;
import com.selahfinance.reflexion.application.port.in.GestionarReflexionUseCase;
import com.selahfinance.reportes.application.port.in.ConsultarReporteFinancieroUseCase;
import com.selahfinance.shared.domain.Periodo;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Módulo de composición: no tiene datos propios. Cada tarjeta es un atajo a su pantalla y
 * nunca duplica ni permite editar el dato (spec 4.3).
 */
@Service
@RequiredArgsConstructor
class InicioService implements ConsultarInicioUseCase {

    private final ConsultarReporteFinancieroUseCase financiero;
    private final ConsultarResumenMayordomiaUseCase mayordomia;
    private final GestionarMetasUseCase metas;
    private final GestionarReflexionUseCase reflexion;
    private final ConsultarBandejaUseCase bandeja;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public InicioResult inicio(UUID usuarioId, UUID hogarId) {
        var periodo = Periodo.de(LocalDate.now(clock));
        return new InicioResult(
                financiero.generar(hogarId, periodo),
                mayordomia.resumen(hogarId, periodo),
                metas.principal(hogarId),
                reflexion.actual(usuarioId, hogarId),
                bandeja.noLeidas(usuarioId));
    }
}
