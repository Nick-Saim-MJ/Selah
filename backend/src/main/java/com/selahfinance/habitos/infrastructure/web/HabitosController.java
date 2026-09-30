package com.selahfinance.habitos.infrastructure.web;

import com.selahfinance.habitos.application.port.in.GestionarHabitosUseCase;
import com.selahfinance.habitos.domain.model.FrecuenciaMeta;
import com.selahfinance.habitos.domain.model.HabitoDelDia;
import com.selahfinance.habitos.domain.model.UnidadHabito;
import com.selahfinance.shared.domain.DimensionMayordomia;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Hábitos", description = "Tiempo, Talento y Templo: lo que el usuario registra en la app")
@RestController
@RequestMapping("/api/v1/habitos")
@RequiredArgsConstructor
class HabitosController {

    private final GestionarHabitosUseCase habitos;

    @Operation(summary = "Hábitos activos con lo registrado hoy y en la semana")
    @GetMapping
    List<HabitoResponse> delDia(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        var u = UsuarioActual.de(jwt);
        return habitos.delDia(u.usuarioId(), u.hogarId(), fecha).stream().map(HabitoResponse::de).toList();
    }

    @Operation(summary = "Registra o corrige el valor de un hábito en un día")
    @PostMapping("/registros")
    HabitoResponse registrar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RegistroRequest r) {
        var u = UsuarioActual.de(jwt);
        return HabitoResponse.de(habitos.registrar(u.usuarioId(), u.hogarId(), r.codigo(), r.fecha(), r.valor(), r.nota()));
    }

    record RegistroRequest(
            @NotBlank String codigo,
            LocalDate fecha,
            @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal valor,
            @Size(max = 250) String nota) {
    }

    record HabitoResponse(String codigo, DimensionMayordomia dimension, String nombre, String descripcion,
            UnidadHabito unidad, FrecuenciaMeta frecuenciaMeta, BigDecimal metaValor, String referenciaBiblica,
            BigDecimal valorHoy, BigDecimal valorSemana, BigDecimal cumplimientoPct) {

        static HabitoResponse de(HabitoDelDia d) {
            var h = d.habito();
            return new HabitoResponse(h.codigo(), h.dimension(), h.nombre(), h.descripcion(), h.unidad(),
                    h.frecuenciaMeta(), h.metaValor(), h.referenciaBiblica(), d.valorHoy(), d.valorSemana(),
                    d.cumplimientoPct());
        }
    }
}
