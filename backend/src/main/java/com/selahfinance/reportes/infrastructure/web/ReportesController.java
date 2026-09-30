package com.selahfinance.reportes.infrastructure.web;

import com.selahfinance.reportes.application.port.in.ConsultarReporteFinancieroUseCase;
import com.selahfinance.reportes.application.port.in.GenerarReporteMayordomiaUseCase;
import com.selahfinance.reportes.domain.model.LineaPresupuesto;
import com.selahfinance.reportes.domain.model.ReporteFinanciero;
import com.selahfinance.reportes.domain.model.ReporteMayordomia;
import com.selahfinance.reportes.domain.model.Semaforo;
import com.selahfinance.shared.domain.DimensionMayordomia;
import com.selahfinance.shared.domain.Periodo;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reportes")
@RestController
@RequestMapping("/api/v1/reportes")
@Validated
@RequiredArgsConstructor
class ReportesController {

    private static final String PERIODO_REGEX = "^\\d{4}-(0[1-9]|1[0-2])$";

    private final ConsultarReporteFinancieroUseCase financiero;
    private final GenerarReporteMayordomiaUseCase mayordomia;
    private final Clock clock;

    @Operation(summary = "Semáforo, ratios y presupuestado vs. real del mes (periodo = YYYY-MM; por defecto el actual)")
    @GetMapping("/financiero")
    FinancieroResponse financiero(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @Pattern(regexp = PERIODO_REGEX) String periodo) {
        var p = periodo(periodo);
        return FinancieroResponse.de(financiero.generar(UsuarioActual.de(jwt).hogarId(), p));
    }

    @Operation(summary = "Reporte integral de mayordomía (Tiempo, Talento, Tesoro, Templo) del mes; se recalcula al pedirlo")
    @GetMapping("/mayordomia")
    MayordomiaResponse mayordomia(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @Pattern(regexp = PERIODO_REGEX) String periodo) {
        var u = UsuarioActual.de(jwt);
        return MayordomiaResponse.de(mayordomia.generar(u.usuarioId(), u.hogarId(), periodo(periodo)));
    }

    private Periodo periodo(String texto) {
        return texto == null ? Periodo.de(LocalDate.now(clock)) : Periodo.parse(texto);
    }

    record FinancieroResponse(String periodo, BigDecimal ingresos, BigDecimal gastos, BigDecimal pagosDeuda,
            BigDecimal aportesMeta, BigDecimal saldoDisponible, BigDecimal apartadoTotal, BigDecimal apartadoPendiente,
            BigDecimal ratioGasto, BigDecimal ratioDeuda, BigDecimal ratioAhorro, Semaforo semaforo,
            List<LineaResponse> categorias, LineaResponse principalDesvio) {

        static FinancieroResponse de(ReporteFinanciero r) {
            var i = r.mes().indicadores();
            return new FinancieroResponse(r.periodo().toString(), i.ingresos().valor(), i.gastos().valor(),
                    i.pagosDeuda().valor(), i.aportesMeta().valor(), r.mes().saldoDisponible().valor(),
                    r.mes().apartadoTotal().valor(), r.mes().apartadoPendiente().valor(), r.ratioGasto(),
                    r.ratioDeuda(), r.ratioAhorro(), r.semaforo().orElse(null),
                    r.lineas().stream().map(LineaResponse::de).toList(),
                    r.principalDesvio().map(LineaResponse::de).orElse(null));
        }
    }

    record LineaResponse(UUID categoriaId, String nombre, String color, BigDecimal planeado, BigDecimal real,
            BigDecimal variacionPct) {

        static LineaResponse de(LineaPresupuesto l) {
            return new LineaResponse(l.categoriaId(), l.nombre(), l.color(), l.planeado().valor(), l.real().valor(),
                    l.variacionPct());
        }
    }

    record MayordomiaResponse(String periodo, BigDecimal tiempo, BigDecimal talento, BigDecimal tesoro,
            BigDecimal templo, BigDecimal global, Semaforo semaforo, Map<String, List<HabitoResponse>> habitos,
            Boolean diezmoAlDia, Instant generadoEn) {

        static MayordomiaResponse de(ReporteMayordomia r) {
            var p = r.puntajes();
            var habitos = new LinkedHashMap<String, List<HabitoResponse>>();
            for (var dimension : DimensionMayordomia.values()) {
                var lista = r.habitos().get(dimension);
                if (lista != null && !lista.isEmpty()) {
                    habitos.put(dimension.name(), lista.stream().map(h -> new HabitoResponse(h.codigo(), h.nombre(),
                            cumplimiento(h.valorRegistrado(), h.metaPeriodo()), h.fuente())).toList());
                }
            }
            return new MayordomiaResponse(r.periodo().toString(), p.tiempo(), p.talento(), p.tesoro(), p.templo(),
                    p.global().orElse(null), r.semaforo().orElse(null), habitos, r.diezmoAlDia(), r.generadoEn());
        }

        private static BigDecimal cumplimiento(BigDecimal valor, BigDecimal meta) {
            if (meta.signum() <= 0) {
                return BigDecimal.ZERO;
            }
            return valor.multiply(BigDecimal.valueOf(100)).divide(meta, 1, RoundingMode.HALF_EVEN)
                    .min(BigDecimal.valueOf(100));
        }
    }

    record HabitoResponse(String codigo, String nombre, BigDecimal cumplimientoPct, String fuente) {
    }
}
