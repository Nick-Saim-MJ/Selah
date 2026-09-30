package com.selahfinance.reportes.application.usecase;

import com.selahfinance.reportes.application.port.in.ConsultarReporteFinancieroUseCase;
import com.selahfinance.reportes.application.port.in.ConsultarReporteMayordomiaUseCase;
import com.selahfinance.reportes.application.port.in.GenerarReporteMayordomiaUseCase;
import com.selahfinance.reportes.application.port.out.CuentasParaReportePort;
import com.selahfinance.reportes.application.port.out.DatosFinancierosPort;
import com.selahfinance.reportes.application.port.out.MetricasDimensionPort;
import com.selahfinance.reportes.application.port.out.ReporteRepositoryPort;
import com.selahfinance.reportes.domain.model.CumplimientoHabito;
import com.selahfinance.reportes.domain.model.PuntajesMayordomia;
import com.selahfinance.reportes.domain.model.ReporteFinanciero;
import com.selahfinance.reportes.domain.model.ReporteMayordomia;
import com.selahfinance.reportes.domain.service.CalculadoraPuntajes;
import com.selahfinance.shared.domain.DimensionMayordomia;
import com.selahfinance.shared.domain.DomainException;
import com.selahfinance.shared.domain.Periodo;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class ReportesService
        implements ConsultarReporteFinancieroUseCase, GenerarReporteMayordomiaUseCase, ConsultarReporteMayordomiaUseCase {

    private static final List<DimensionMayordomia> DIMENSIONES_DE_HABITOS = List.of(
            DimensionMayordomia.TIEMPO, DimensionMayordomia.TALENTO, DimensionMayordomia.TEMPLO);

    private final CalculadoraPuntajes calculadora = new CalculadoraPuntajes();

    private final DatosFinancierosPort datos;
    private final ReporteRepositoryPort reportes;
    private final CuentasParaReportePort cuentas;
    /** Todas las fuentes de métricas: la local (app) y las APIs de otros equipos. */
    private final List<MetricasDimensionPort> fuentes;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public ReporteFinanciero generar(UUID hogarId, Periodo periodo) {
        return ReporteFinanciero.armar(periodo, datos.mes(hogarId, periodo), datos.presupuestoVsReal(hogarId, periodo));
    }

    @Override
    @Transactional
    public ReporteMayordomia generar(UUID usuarioId, UUID hogarId, Periodo periodo) {
        LocalDate hoy = LocalDate.now(clock);
        if (periodo.inicio().isAfter(hoy)) {
            throw new DomainException("PERIODO_FUTURO", "No se puede generar el reporte de un mes que aún no empieza");
        }
        // En el mes en curso solo se mide hasta hoy: las metas de los días que faltan no cuentan en contra.
        LocalDate hasta = periodo.fin().isAfter(hoy) ? hoy : periodo.fin();

        var mes = datos.mes(hogarId, periodo);
        var habitos = new EnumMap<DimensionMayordomia, List<CumplimientoHabito>>(DimensionMayordomia.class);
        var puntaje = new EnumMap<DimensionMayordomia, BigDecimal>(DimensionMayordomia.class);
        for (var dimension : DIMENSIONES_DE_HABITOS) {
            var lista = fuentes.stream()
                    .flatMap(f -> f.obtener(usuarioId, dimension, periodo.inicio(), hasta).stream())
                    .toList();
            habitos.put(dimension, lista);
            puntaje.put(dimension, calculadora.puntajeHabitos(lista));
        }
        var puntajes = new PuntajesMayordomia(puntaje.get(DimensionMayordomia.TIEMPO),
                puntaje.get(DimensionMayordomia.TALENTO), calculadora.puntajeTesoro(mes.indicadores()),
                puntaje.get(DimensionMayordomia.TEMPLO));

        var reporte = new ReporteMayordomia(usuarioId, hogarId, periodo, puntajes, Map.copyOf(habitos),
                diezmoAlDia(mes), clock.instant(), CalculadoraPuntajes.VERSION);
        this.reportes.guardar(reporte);
        return reporte;
    }

    @Override
    public int generarParaTodos(Periodo periodo) {
        int generados = 0;
        for (var cuenta : cuentas.activas()) {
            try {
                generar(cuenta.usuarioId(), cuenta.hogarId(), periodo);
                generados++;
            } catch (RuntimeException e) {
                // Una cuenta con datos raros no debe frenar el resto
                log.warn("No se pudo generar el reporte 4T de {}: {}", cuenta.usuarioId(), e.getMessage());
            }
        }
        return generados;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReporteMayordomia> existente(UUID usuarioId, UUID hogarId, Periodo periodo) {
        return reportes.porUsuarioYPeriodo(usuarioId, hogarId, periodo);
    }

    private static Boolean diezmoAlDia(com.selahfinance.reportes.domain.model.ResumenMes mes) {
        var i = mes.indicadores();
        return i.diezmoApartado().esPositivo() ? i.diezmoEntregado().compareTo(i.diezmoApartado()) >= 0 : null;
    }
}
