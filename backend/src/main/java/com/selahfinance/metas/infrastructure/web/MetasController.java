package com.selahfinance.metas.infrastructure.web;

import com.selahfinance.metas.application.port.in.GestionarMetasUseCase;
import com.selahfinance.metas.application.port.in.GestionarMetasUseCase.Comando;
import com.selahfinance.metas.domain.model.EstadoMeta;
import com.selahfinance.metas.domain.model.MetaConProgreso;
import com.selahfinance.metas.domain.model.TipoMeta;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Metas de ahorro", description = "Para aportar, registrar un movimiento APORTE_META con metaId")
@RestController
@RequestMapping("/api/v1/metas")
@RequiredArgsConstructor
class MetasController {

    private final GestionarMetasUseCase metas;
    private final Clock clock;

    @Operation(summary = "Metas con progreso (la principal primero)")
    @GetMapping
    List<MetaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return metas.listar(hogar(jwt)).stream().map(this::respuesta).toList();
    }

    @Operation(summary = "Crea una meta (la primera queda como principal)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    MetaResponse crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MetaRequest r) {
        return respuesta(metas.crear(hogar(jwt), r.aComando()));
    }

    @Operation(summary = "Edita una meta")
    @PutMapping("/{id}")
    MetaResponse actualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody MetaRequest r) {
        return respuesta(metas.actualizar(hogar(jwt), id, r.aComando()));
    }

    @Operation(summary = "Marca la meta como principal (la que se muestra en Inicio)")
    @PostMapping("/{id}/principal")
    MetaResponse principal(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return respuesta(metas.marcarPrincipal(hogar(jwt), id));
    }

    @Operation(summary = "Cancela la meta")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        metas.cancelar(hogar(jwt), id);
    }

    private MetaResponse respuesta(MetaConProgreso m) {
        var meta = m.meta();
        var sugerido = m.aporteMensualSugerido(LocalDate.now(clock));
        return new MetaResponse(meta.id(), meta.nombre(), meta.proposito(), meta.tipo(), meta.montoObjetivo().valor(),
                m.montoActual().valor(), m.porcentaje(), m.restante().valor(), meta.fechaObjetivo(),
                sugerido == null ? null : sugerido.valor(), meta.esPrincipal(), meta.estado());
    }

    private static UUID hogar(Jwt jwt) {
        return UsuarioActual.de(jwt).hogarId();
    }

    record MetaRequest(
            @NotBlank @Size(max = 80) String nombre,
            @NotBlank @Size(max = 250) String proposito,
            TipoMeta tipo,
            @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal montoObjetivo,
            LocalDate fechaObjetivo) {

        Comando aComando() {
            return new Comando(nombre, proposito, tipo, montoObjetivo, fechaObjetivo);
        }
    }

    record MetaResponse(UUID id, String nombre, String proposito, TipoMeta tipo, BigDecimal montoObjetivo,
            BigDecimal montoActual, BigDecimal porcentaje, BigDecimal restante, LocalDate fechaObjetivo,
            BigDecimal aporteMensualSugerido, boolean esPrincipal, EstadoMeta estado) {
    }
}
