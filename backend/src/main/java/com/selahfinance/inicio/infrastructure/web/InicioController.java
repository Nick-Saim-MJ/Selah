package com.selahfinance.inicio.infrastructure.web;

import com.selahfinance.inicio.application.dto.InicioResult;
import com.selahfinance.inicio.application.port.in.ConsultarInicioUseCase;
import com.selahfinance.reportes.domain.model.LineaPresupuesto;
import com.selahfinance.reportes.domain.model.Semaforo;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inicio")
@RestController
@RequestMapping("/api/v1/inicio")
@RequiredArgsConstructor
class InicioController {

    private final ConsultarInicioUseCase inicio;

    @Operation(summary = "Pantalla Inicio: semáforo, saldo disponible (ya sin diezmo ni ofrenda), diezmo pendiente, meta principal")
    @GetMapping
    InicioResponse inicio(@AuthenticationPrincipal Jwt jwt) {
        var u = UsuarioActual.de(jwt);
        return InicioResponse.de(inicio.inicio(u.usuarioId(), u.hogarId()));
    }

    record InicioResponse(String periodo, Semaforo semaforo, BigDecimal ratioGasto, BigDecimal ratioDeuda,
            BigDecimal ratioAhorro, BigDecimal ingresos, BigDecimal gastos, BigDecimal saldoDisponible,
            BigDecimal apartadoTotal, DesvioResponse principalDesvio, DiezmoResponse diezmo, MetaResponse metaPrincipal,
            boolean reflexionVisible, boolean reflexionRespondida, long notificacionesNoLeidas) {

        static InicioResponse de(InicioResult r) {
            var f = r.financiero();
            var i = f.mes().indicadores();
            var m = r.mayordomia();
            return new InicioResponse(f.periodo().toString(), f.semaforo().orElse(null), f.ratioGasto(), f.ratioDeuda(),
                    f.ratioAhorro(), i.ingresos().valor(), i.gastos().valor(), f.mes().saldoDisponible().valor(),
                    f.mes().apartadoTotal().valor(),
                    f.principalDesvio().map(DesvioResponse::de).orElse(null),
                    new DiezmoResponse(m.diezmoApartado().valor(), m.diezmoPendiente().valor(),
                            m.ofrendaPendiente().valor(), m.diezmoAlDia()),
                    r.metaPrincipal().map(mp -> new MetaResponse(mp.meta().id(), mp.meta().nombre(),
                            mp.montoActual().valor(), mp.meta().montoObjetivo().valor(), mp.porcentaje())).orElse(null),
                    r.reflexion().visible(), r.reflexion().respuesta() != null, r.notificacionesNoLeidas());
        }
    }

    record DesvioResponse(String categoria, BigDecimal variacionPct) {

        static DesvioResponse de(LineaPresupuesto l) {
            return new DesvioResponse(l.nombre(), l.variacionPct());
        }
    }

    record DiezmoResponse(BigDecimal apartado, BigDecimal pendiente, BigDecimal ofrendaPendiente, boolean alDia) {
    }

    record MetaResponse(UUID id, String nombre, BigDecimal montoActual, BigDecimal montoObjetivo, BigDecimal porcentaje) {
    }
}
