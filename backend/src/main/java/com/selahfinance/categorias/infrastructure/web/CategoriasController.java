package com.selahfinance.categorias.infrastructure.web;

import com.selahfinance.categorias.application.port.in.GestionarCategoriasUseCase;
import com.selahfinance.categorias.application.port.in.GestionarCategoriasUseCase.Comando;
import com.selahfinance.categorias.application.port.in.GestionarFuentesIngresoUseCase;
import com.selahfinance.categorias.domain.model.Categoria;
import com.selahfinance.categorias.domain.model.FuenteIngreso;
import com.selahfinance.categorias.domain.model.TipoCategoria;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Categorías y fuentes de ingreso")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
class CategoriasController {

    private final GestionarCategoriasUseCase categorias;
    private final GestionarFuentesIngresoUseCase fuentes;

    // ---------- Categorías

    @Operation(summary = "Categorías activas del hogar (tipo GASTO por defecto)")
    @GetMapping("/categorias")
    List<CategoriaResponse> listar(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "GASTO") TipoCategoria tipo) {
        return categorias.listar(hogar(jwt), tipo).stream().map(CategoriaResponse::de).toList();
    }

    @Operation(summary = "Crea una categoría")
    @PostMapping("/categorias")
    @ResponseStatus(HttpStatus.CREATED)
    CategoriaResponse crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CategoriaRequest r) {
        return CategoriaResponse.de(categorias.crear(hogar(jwt), r.aComando()));
    }

    @Operation(summary = "Edita nombre, color o presupuesto mensual")
    @PutMapping("/categorias/{id}")
    CategoriaResponse actualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            @Valid @RequestBody CategoriaRequest r) {
        return CategoriaResponse.de(categorias.actualizar(hogar(jwt), id, r.aComando()));
    }

    @Operation(summary = "Oculta la categoría (los movimientos antiguos la conservan)")
    @DeleteMapping("/categorias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void desactivar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        categorias.desactivar(hogar(jwt), id);
    }

    // ---------- Fuentes de ingreso

    @Operation(summary = "Fuentes de ingreso activas con su monto estimado mensual")
    @GetMapping("/fuentes-ingreso")
    List<FuenteResponse> listarFuentes(@AuthenticationPrincipal Jwt jwt) {
        return fuentes.listar(hogar(jwt)).stream().map(FuenteResponse::de).toList();
    }

    @Operation(summary = "Crea una fuente de ingreso")
    @PostMapping("/fuentes-ingreso")
    @ResponseStatus(HttpStatus.CREATED)
    FuenteResponse crearFuente(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody FuenteRequest r) {
        return FuenteResponse.de(fuentes.crear(hogar(jwt), r.nombre(), r.montoEstimadoMensual()));
    }

    @Operation(summary = "Edita una fuente de ingreso")
    @PutMapping("/fuentes-ingreso/{id}")
    FuenteResponse actualizarFuente(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            @Valid @RequestBody FuenteRequest r) {
        return FuenteResponse.de(fuentes.actualizar(hogar(jwt), id, r.nombre(), r.montoEstimadoMensual()));
    }

    @Operation(summary = "Oculta una fuente de ingreso")
    @DeleteMapping("/fuentes-ingreso/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void desactivarFuente(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        fuentes.desactivar(hogar(jwt), id);
    }

    private static UUID hogar(Jwt jwt) {
        return UsuarioActual.de(jwt).hogarId();
    }

    record CategoriaRequest(
            @NotBlank @Size(max = 60) String nombre,
            TipoCategoria tipo,
            @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Formato #RRGGBB") String color,
            @Size(max = 40) String icono,
            @DecimalMin("0") @Digits(integer = 12, fraction = 2) BigDecimal presupuestoMensual) {

        Comando aComando() {
            return new Comando(nombre, tipo, color, icono, presupuestoMensual);
        }
    }

    record CategoriaResponse(UUID id, String nombre, TipoCategoria tipo, String color, String icono,
            BigDecimal presupuestoMensual, int orden) {

        static CategoriaResponse de(Categoria c) {
            return new CategoriaResponse(c.id(), c.nombre(), c.tipo(), c.color(), c.icono(),
                    c.presupuestoMensual().valor(), c.orden());
        }
    }

    record FuenteRequest(
            @NotBlank @Size(max = 60) String nombre,
            @DecimalMin("0") @Digits(integer = 12, fraction = 2) BigDecimal montoEstimadoMensual) {
    }

    record FuenteResponse(UUID id, String nombre, BigDecimal montoEstimadoMensual) {

        static FuenteResponse de(FuenteIngreso f) {
            return new FuenteResponse(f.id(), f.nombre(), f.montoEstimadoMensual().valor());
        }
    }
}
