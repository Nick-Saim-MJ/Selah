package com.selahfinance.reflexion.infrastructure.web;

import com.selahfinance.reflexion.application.port.in.GestionarReflexionUseCase;
import com.selahfinance.reflexion.domain.model.TarjetaReflexion;
import com.selahfinance.shared.domain.DimensionMayordomia;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reflexión semanal")
@RestController
@RequestMapping("/api/v1/reflexion")
@RequiredArgsConstructor
class ReflexionController {

    private final GestionarReflexionUseCase reflexion;

    @Operation(summary = "Tarjeta de la semana: resumen, pregunta y mi respuesta (visible = viernes tarde y sábado)")
    @GetMapping("/actual")
    TarjetaResponse actual(@AuthenticationPrincipal Jwt jwt) {
        var u = UsuarioActual.de(jwt);
        return TarjetaResponse.de(reflexion.actual(u.usuarioId(), u.hogarId()));
    }

    @Operation(summary = "Guarda mi respuesta (opcional) a la pregunta de la semana")
    @PutMapping("/actual")
    TarjetaResponse responder(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RespuestaRequest r) {
        var u = UsuarioActual.de(jwt);
        return TarjetaResponse.de(reflexion.responder(u.usuarioId(), u.hogarId(), r.respuesta()));
    }

    record RespuestaRequest(@Size(max = 2000) String respuesta) {
    }

    record TarjetaResponse(boolean visible, LocalDate semanaInicio, PreguntaResponse pregunta, String respuesta,
            BigDecimal entro, BigDecimal gasto, Boolean diezmoAlDia) {

        static TarjetaResponse de(TarjetaReflexion t) {
            var p = t.pregunta();
            return new TarjetaResponse(t.visible(), t.semanaInicio(),
                    new PreguntaResponse(p.id(), p.texto(), p.referenciaBiblica(), p.dimension()), t.respuesta(),
                    t.resumen().entro().valor(), t.resumen().gasto().valor(), t.resumen().diezmoAlDia());
        }
    }

    record PreguntaResponse(short id, String texto, String referenciaBiblica, DimensionMayordomia dimension) {
    }
}
