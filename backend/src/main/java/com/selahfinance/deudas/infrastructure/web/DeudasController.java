package com.selahfinance.deudas.infrastructure.web;

import com.selahfinance.deudas.application.port.in.GestionarDeudasUseCase;
import com.selahfinance.deudas.application.port.in.GestionarDeudasUseCase.ComandoCrear;
import com.selahfinance.deudas.domain.model.Deuda;
import com.selahfinance.deudas.domain.model.EstadoDeuda;
import com.selahfinance.deudas.domain.model.ResumenDeudas;
import com.selahfinance.deudas.domain.service.CalculadoraDeuda;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Deudas", description = "Para pagar, registrar un movimiento PAGO_DEUDA con deudaId")
@RestController
@RequestMapping("/api/v1/deudas")
@RequiredArgsConstructor
class DeudasController {

    private final GestionarDeudasUseCase deudas;

    @Operation(summary = "Deudas activas, cuota total y alerta de sobreendeudamiento (cuotas > 40% del ingreso)")
    @GetMapping
    ResumenResponse resumen(@AuthenticationPrincipal Jwt jwt) {
        return ResumenResponse.de(deudas.resumen(hogar(jwt)));
    }

    @Operation(summary = "Registra una deuda; la cuota se calcula (sistema francés)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DeudaResponse crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CrearRequest r) {
        return DeudaResponse.de(deudas.crear(hogar(jwt), new ComandoCrear(r.nombre(), r.acreedor(), r.montoOriginal(),
                r.saldoActual(), r.tasaAnual(), r.plazoMeses(), r.fechaInicio(), r.diaPago())));
    }

    @Operation(summary = "Edita nombre, acreedor o día de pago")
    @PutMapping("/{id}")
    DeudaResponse actualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            @Valid @RequestBody ActualizarRequest r) {
        return DeudaResponse.de(deudas.actualizar(hogar(jwt), id, r.nombre(), r.acreedor(), r.diaPago()));
    }

    private static UUID hogar(Jwt jwt) {
        return UsuarioActual.de(jwt).hogarId();
    }

    record CrearRequest(
            @NotBlank @Size(max = 80) String nombre,
            @Size(max = 80) String acreedor,
            @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal montoOriginal,
            @DecimalMin("0") @Digits(integer = 12, fraction = 2) BigDecimal saldoActual,
            @NotNull @DecimalMin("0") @Digits(integer = 3, fraction = 4) BigDecimal tasaAnual,
            @Min(1) @Max(600) int plazoMeses,
            LocalDate fechaInicio,
            @Min(1) @Max(31) Integer diaPago) {
    }

    record ActualizarRequest(
            @NotBlank @Size(max = 80) String nombre,
            @Size(max = 80) String acreedor,
            @Min(1) @Max(31) Integer diaPago) {
    }

    record DeudaResponse(UUID id, String nombre, String acreedor, BigDecimal montoOriginal, BigDecimal saldoActual,
            BigDecimal tasaAnual, int plazoMeses, BigDecimal cuotaMensual, Integer mesesRestantes,
            BigDecimal porcentajePagado, Integer diaPago, EstadoDeuda estado) {

        static DeudaResponse de(Deuda d) {
            var meses = d.mesesRestantes();
            return new DeudaResponse(d.id(), d.nombre(), d.acreedor(), d.montoOriginal().valor(), d.saldoActual().valor(),
                    d.tasaAnual(), d.plazoMeses(), d.cuotaMensual().valor(), meses.isPresent() ? meses.getAsInt() : null,
                    d.porcentajePagado(), d.diaPago(), d.estado());
        }
    }

    record ResumenResponse(List<DeudaResponse> deudas, BigDecimal cuotaTotalMensual, BigDecimal ingresoMensualBase,
            BigDecimal porcentajeCuotasSobreIngreso, boolean superaLimite, BigDecimal limitePorcentaje) {

        static ResumenResponse de(ResumenDeudas r) {
            return new ResumenResponse(r.deudas().stream().map(DeudaResponse::de).toList(),
                    r.cuotaTotalMensual().valor(), r.ingresoMensualBase().valor(), r.porcentajeCuotasSobreIngreso(),
                    r.superaLimite(), CalculadoraDeuda.LIMITE_ENDEUDAMIENTO_PCT);
        }
    }
}
