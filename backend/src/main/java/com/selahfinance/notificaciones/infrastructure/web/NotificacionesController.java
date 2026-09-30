package com.selahfinance.notificaciones.infrastructure.web;

import com.selahfinance.notificaciones.application.port.in.ConsultarBandejaUseCase;
import com.selahfinance.notificaciones.application.port.in.GestionarPreferenciasUseCase;
import com.selahfinance.notificaciones.domain.model.CategoriaNotificacion;
import com.selahfinance.notificaciones.domain.model.Notificacion;
import com.selahfinance.notificaciones.domain.model.PreferenciasNotificacion;
import com.selahfinance.notificaciones.domain.model.TipoNotificacion;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notificaciones", description = "Bandeja dentro de la app. Los avisos de consumo se posponen en Modo Sábado.")
@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
class NotificacionesController {

    private final ConsultarBandejaUseCase bandeja;
    private final GestionarPreferenciasUseCase preferencias;

    @Operation(summary = "Bandeja: avisos visibles del más nuevo al más viejo, con contador de no leídos")
    @GetMapping
    BandejaResponse listar(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "30") int tamanio) {
        var usuarioId = UsuarioActual.de(jwt).usuarioId();
        var resultado = bandeja.bandeja(usuarioId, pagina, tamanio);
        return new BandejaResponse(resultado.contenido().stream().map(NotificacionResponse::de).toList(),
                bandeja.noLeidas(usuarioId), resultado.totalPaginas());
    }

    @Operation(summary = "Solo el contador de no leídos (para el globo de la campana)")
    @GetMapping("/no-leidas")
    ContadorResponse noLeidas(@AuthenticationPrincipal Jwt jwt) {
        return new ContadorResponse(bandeja.noLeidas(UsuarioActual.de(jwt).usuarioId()));
    }

    @Operation(summary = "Marca un aviso como leído")
    @PostMapping("/{id}/leida")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void marcarLeida(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        bandeja.marcarLeida(UsuarioActual.de(jwt).usuarioId(), id);
    }

    @Operation(summary = "Marca todos como leídos")
    @PostMapping("/leidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void marcarTodasLeidas(@AuthenticationPrincipal Jwt jwt) {
        bandeja.marcarTodasLeidas(UsuarioActual.de(jwt).usuarioId());
    }

    @Operation(summary = "Qué avisos quiero recibir")
    @GetMapping("/preferencias")
    PreferenciasResponse preferencias(@AuthenticationPrincipal Jwt jwt) {
        return PreferenciasResponse.de(preferencias.obtener(UsuarioActual.de(jwt).usuarioId()));
    }

    @Operation(summary = "Actualiza qué avisos quiero recibir")
    @PutMapping("/preferencias")
    PreferenciasResponse actualizarPreferencias(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PreferenciasRequest r) {
        return PreferenciasResponse.de(preferencias.actualizar(new PreferenciasNotificacion(
                UsuarioActual.de(jwt).usuarioId(), r.recordatorioDiezmo(), r.recordatorioDeudas(), r.resumenSemanal(),
                r.reflexionSabado())));
    }

    record PreferenciasRequest(boolean recordatorioDiezmo, boolean recordatorioDeudas, boolean resumenSemanal,
            boolean reflexionSabado) {
    }

    record PreferenciasResponse(boolean recordatorioDiezmo, boolean recordatorioDeudas, boolean resumenSemanal,
            boolean reflexionSabado) {

        static PreferenciasResponse de(PreferenciasNotificacion p) {
            return new PreferenciasResponse(p.recordatorioDiezmo(), p.recordatorioDeudas(), p.resumenSemanal(),
                    p.reflexionSabado());
        }
    }

    record ContadorResponse(long noLeidas) {
    }

    record BandejaResponse(List<NotificacionResponse> items, long noLeidas, int totalPaginas) {
    }

    record NotificacionResponse(UUID id, TipoNotificacion tipo, CategoriaNotificacion categoria, String titulo,
            String cuerpo, Map<String, String> datos, Instant fecha, boolean leida) {

        static NotificacionResponse de(Notificacion n) {
            return new NotificacionResponse(n.id(), n.tipo(), n.categoria(), n.titulo(), n.cuerpo(), n.datos(),
                    n.programadaPara(), n.leida());
        }
    }
}
