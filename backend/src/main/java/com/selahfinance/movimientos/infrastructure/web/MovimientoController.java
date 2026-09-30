package com.selahfinance.movimientos.infrastructure.web;

import com.selahfinance.movimientos.application.port.in.ConsultarMovimientosUseCase;
import com.selahfinance.movimientos.application.port.in.EliminarMovimientoUseCase;
import com.selahfinance.movimientos.application.port.in.RegistrarMovimientoUseCase;
import com.selahfinance.movimientos.domain.model.OrigenMovimiento;
import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import com.selahfinance.shared.domain.DomainException;
import com.selahfinance.shared.infrastructure.web.PaginaResponse;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Movimientos", description = "Fuente única de captura de ingresos, gastos, pagos y aportes")
@RestController
@RequestMapping("/api/v1/movimientos")
@RequiredArgsConstructor
class MovimientoController {

    private final RegistrarMovimientoUseCase registrar;
    private final ConsultarMovimientosUseCase consultar;
    private final EliminarMovimientoUseCase eliminar;

    @Operation(summary = "Registra un movimiento. Si es INGRESO, se apartan automáticamente diezmo y ofrenda")
    @PostMapping
    ResponseEntity<MovimientoResponse> registrar(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RegistrarMovimientoRequest r) {
        if (r.tipo() == TipoMovimiento.DIEZMO) {
            throw new DomainException("USAR_ENTREGAS", "El diezmo se entrega desde POST /api/v1/mayordomia/entregas");
        }
        var usuario = UsuarioActual.de(jwt);
        var movimiento = registrar.registrar(new RegistrarMovimientoUseCase.Comando(
                usuario.hogarId(), usuario.usuarioId(), r.tipo(), r.monto(), r.fecha(), r.descripcion(), r.nota(),
                r.categoriaId(), r.fuenteIngresoId(), r.metaId(), r.deudaId(), r.destinoOfrendaId(),
                r.esPresupuestado(), OrigenMovimiento.APP, null));
        return ResponseEntity.created(URI.create("/api/v1/movimientos/" + movimiento.id()))
                .body(MovimientoResponse.de(movimiento));
    }

    @Operation(summary = "Lista movimientos con filtros (tipo, rango de fechas, categoría, texto)")
    @GetMapping
    PaginaResponse<MovimientoResponse> listar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(name = "q", required = false) String texto,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {
        var hogarId = UsuarioActual.de(jwt).hogarId();
        var resultado = consultar.buscar(new ConsultarMovimientosUseCase.Filtro(
                hogarId, tipo, desde, hasta, categoriaId, texto, pagina, tamanio));
        return PaginaResponse.de(resultado, MovimientoResponse::de);
    }

    @Operation(summary = "Detalle de un movimiento")
    @GetMapping("/{id}")
    MovimientoResponse obtener(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return MovimientoResponse.de(consultar.obtener(UsuarioActual.de(jwt).hogarId(), id));
    }

    @Operation(summary = "Elimina (lógicamente) un movimiento")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void eliminar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        eliminar.eliminar(UsuarioActual.de(jwt).hogarId(), id);
    }
}
