package com.selahfinance.mayordomia.infrastructure.web;

import com.selahfinance.mayordomia.application.port.in.ConsultarResumenMayordomiaUseCase;
import com.selahfinance.mayordomia.application.port.in.EntregarMayordomiaUseCase;
import com.selahfinance.mayordomia.application.port.in.SimularDesgloseUseCase;
import com.selahfinance.mayordomia.domain.model.DesgloseIngreso;
import com.selahfinance.mayordomia.domain.model.ResumenMayordomia;
import com.selahfinance.mayordomia.domain.model.TipoApartado;
import com.selahfinance.shared.domain.Periodo;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Diezmos y ofrendas")
@RestController
@RequestMapping("/api/v1/mayordomia")
@RequiredArgsConstructor
class MayordomiaController {

    private final SimularDesgloseUseCase simular;
    private final ConsultarResumenMayordomiaUseCase resumen;
    private final EntregarMayordomiaUseCase entregar;
    private final Clock clock;

    @Operation(summary = "Vista previa: cuánto de un ingreso es diezmo, ofrenda y disponible (no guarda)")
    @PostMapping("/simulacion")
    DesgloseResponse simular(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SimulacionRequest r) {
        return DesgloseResponse.de(simular.simular(UsuarioActual.de(jwt).hogarId(), r.monto()));
    }

    @Operation(summary = "Diezmo y ofrenda apartados/pendientes del mes (periodo = YYYY-MM; por defecto el actual)")
    @GetMapping("/resumen")
    ResumenResponse resumen(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @Pattern(regexp = "^\\d{4}-\\d{2}$") String periodo) {
        var p = periodo == null ? Periodo.de(LocalDate.now(clock)) : Periodo.parse(periodo);
        return ResumenResponse.de(resumen.resumen(UsuarioActual.de(jwt).hogarId(), p));
    }

    @Operation(summary = "Fidelidad mes a mes (por defecto los últimos 6 meses, hasta el mes actual)")
    @GetMapping("/historial")
    List<ResumenResponse> historial(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "6") int meses) {
        return resumen.historial(UsuarioActual.de(jwt).hogarId(), Periodo.de(LocalDate.now(clock)), meses).stream()
                .map(ResumenResponse::de).toList();
    }

    @Operation(summary = "Marca como entregado todo el diezmo u ofrenda pendiente del periodo")
    @PostMapping("/entregas")
    @ResponseStatus(HttpStatus.CREATED)
    EntregaResponse entregar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody EntregaRequest r) {
        var usuario = UsuarioActual.de(jwt);
        var resultado = entregar.entregar(new EntregarMayordomiaUseCase.Comando(
                usuario.hogarId(), usuario.usuarioId(), r.tipo(), Periodo.parse(r.periodo()),
                r.fechaEntrega() == null ? LocalDate.now(clock) : r.fechaEntrega()));
        return new EntregaResponse(resultado.movimientoId(), resultado.montoEntregado().valor(),
                resultado.apartadosEntregados());
    }

    record SimulacionRequest(@NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal monto) {
    }

    record EntregaRequest(
            @NotNull TipoApartado tipo,
            @NotNull @Pattern(regexp = "^\\d{4}-\\d{2}$") String periodo,
            LocalDate fechaEntrega) {
    }

    record DesgloseResponse(BigDecimal ingreso, BigDecimal pctDiezmo, BigDecimal diezmo, BigDecimal pctOfrenda,
            BigDecimal ofrenda, BigDecimal disponible) {

        static DesgloseResponse de(DesgloseIngreso d) {
            return new DesgloseResponse(d.ingreso().valor(), d.pctDiezmo(), d.diezmo().valor(), d.pctOfrenda(),
                    d.ofrenda().valor(), d.disponible().valor());
        }
    }

    record ResumenResponse(String periodo, BigDecimal diezmoApartado, BigDecimal diezmoPendiente,
            BigDecimal ofrendaApartada, BigDecimal ofrendaPendiente, boolean diezmoAlDia) {

        static ResumenResponse de(ResumenMayordomia r) {
            return new ResumenResponse(r.periodo().toString(), r.diezmoApartado().valor(), r.diezmoPendiente().valor(),
                    r.ofrendaApartada().valor(), r.ofrendaPendiente().valor(), r.diezmoAlDia());
        }
    }

    record EntregaResponse(UUID movimientoId, BigDecimal montoEntregado, int apartadosEntregados) {
    }
}
