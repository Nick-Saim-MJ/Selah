package com.selahfinance.congregacion.infrastructure.web;

import com.selahfinance.congregacion.application.port.in.ConsultarCongregacionUseCase;
import com.selahfinance.congregacion.domain.model.ReporteCompartido;
import com.selahfinance.congregacion.domain.model.ResumenCongregacion;
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

/** Solo PASTOR. Nunca devuelve montos de dinero ni movimientos. */
@Tag(name = "Pastor · Congregación")
@RestController
@RequestMapping("/api/v1/pastor")
@Validated
@RequiredArgsConstructor
class PastorController {

    private static final String PERIODO_REGEX = "^\\d{4}-(0[1-9]|1[0-2])$";

    private final ConsultarCongregacionUseCase congregacion;
    private final Clock clock;

    @Operation(summary = "Totales anónimos de mi congregación (miembros, fidelidad en el diezmo, puntajes 4T promedio)")
    @GetMapping("/congregacion")
    ResumenResponse resumen(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @Pattern(regexp = PERIODO_REGEX) String periodo) {
        return ResumenResponse.de(congregacion.resumen(UsuarioActual.de(jwt).usuarioId(), periodo(periodo)));
    }

    @Operation(summary = "Reportes de los hermanos que decidieron compartir el suyo conmigo")
    @GetMapping("/reportes-compartidos")
    List<CompartidoResponse> compartidos(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @Pattern(regexp = PERIODO_REGEX) String periodo) {
        return congregacion.reportesCompartidos(UsuarioActual.de(jwt).usuarioId(), periodo(periodo)).stream()
                .map(CompartidoResponse::de).toList();
    }

    private Periodo periodo(String texto) {
        return texto == null ? Periodo.de(LocalDate.now(clock)) : Periodo.parse(texto);
    }

    record ResumenResponse(String periodo, int miembrosActivos, int miembrosConReporte, int minimoAnonimato,
            boolean datosSuficientes, BigDecimal fidelidadDiezmoPct, BigDecimal tiempo, BigDecimal talento,
            BigDecimal tesoro, BigDecimal templo, BigDecimal global) {

        static ResumenResponse de(ResumenCongregacion r) {
            return new ResumenResponse(r.periodo().toString(), r.miembrosActivos(), r.miembrosConReporte(),
                    r.minimoAnonimato(), r.datosSuficientes(), r.fidelidadDiezmoPct(), r.tiempo(), r.talento(),
                    r.tesoro(), r.templo(), r.global());
        }
    }

    record CompartidoResponse(UUID usuarioId, String nombre, BigDecimal tiempo, BigDecimal talento, BigDecimal tesoro,
            BigDecimal templo, BigDecimal global, Semaforo semaforo, Boolean diezmoAlDia,
            Map<String, List<HabitoResponse>> habitos) {

        static CompartidoResponse de(ReporteCompartido c) {
            var r = c.reporte();
            var p = r.puntajes();
            var habitos = new LinkedHashMap<String, List<HabitoResponse>>();
            for (var d : DimensionMayordomia.values()) {
                var lista = r.habitos().get(d);
                if (lista != null && !lista.isEmpty()) {
                    habitos.put(d.name(), lista.stream().map(h -> new HabitoResponse(h.nombre(),
                            h.metaPeriodo().signum() <= 0 ? BigDecimal.ZERO
                                    : h.valorRegistrado().multiply(BigDecimal.valueOf(100))
                                            .divide(h.metaPeriodo(), 1, RoundingMode.HALF_EVEN)
                                            .min(BigDecimal.valueOf(100)))).toList());
                }
            }
            return new CompartidoResponse(c.usuarioId(), c.nombre(), p.tiempo(), p.talento(), p.tesoro(), p.templo(),
                    p.global().orElse(null), r.semaforo().orElse(null), r.diezmoAlDia(), habitos);
        }
    }

    record HabitoResponse(String nombre, BigDecimal cumplimientoPct) {
    }
}
