package com.selahfinance.hogar.infrastructure.web;

import com.selahfinance.hogar.application.port.in.ConfiguracionMayordomiaUseCase;
import com.selahfinance.hogar.domain.model.ConfiguracionMayordomia;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Hogar")
@RestController
@RequestMapping("/api/v1/hogar/configuracion-mayordomia")
@RequiredArgsConstructor
class ConfiguracionMayordomiaController {

    private final ConfiguracionMayordomiaUseCase configuracion;

    @Operation(summary = "Configuración de diezmo, ofrenda y Modo Sábado del hogar")
    @GetMapping
    ConfiguracionResponse obtener(@AuthenticationPrincipal Jwt jwt) {
        return ConfiguracionResponse.de(configuracion.obtener(UsuarioActual.de(jwt).hogarId()));
    }

    @Operation(summary = "Actualiza la configuración de mayordomía")
    @PutMapping
    ConfiguracionResponse actualizar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ConfiguracionRequest r) {
        var hogarId = UsuarioActual.de(jwt).hogarId();
        return ConfiguracionResponse.de(configuracion.actualizar(new ConfiguracionMayordomia(
                hogarId, r.pctDiezmo(), r.ofrendaActiva(), r.pctOfrenda(), r.diaEntregaDiezmo(), r.modoSabadoActivo())));
    }

    record ConfiguracionRequest(
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal pctDiezmo,
            boolean ofrendaActiva,
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal pctOfrenda,
            @Min(1) @Max(31) Integer diaEntregaDiezmo,
            boolean modoSabadoActivo) {
    }

    record ConfiguracionResponse(
            BigDecimal pctDiezmo,
            boolean ofrendaActiva,
            BigDecimal pctOfrenda,
            Integer diaEntregaDiezmo,
            boolean modoSabadoActivo) {

        static ConfiguracionResponse de(ConfiguracionMayordomia c) {
            return new ConfiguracionResponse(c.pctDiezmo(), c.ofrendaActiva(), c.pctOfrenda(), c.diaEntregaDiezmo(),
                    c.modoSabadoActivo());
        }
    }
}
