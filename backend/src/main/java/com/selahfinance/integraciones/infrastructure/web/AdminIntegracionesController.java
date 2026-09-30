package com.selahfinance.integraciones.infrastructure.web;

import com.selahfinance.integraciones.application.port.in.GestionarClientesApiUseCase;
import com.selahfinance.integraciones.application.port.in.GestionarWebhooksUseCase;
import com.selahfinance.integraciones.domain.model.ClienteApi;
import com.selahfinance.integraciones.domain.model.SuscripcionWebhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Solo ADMIN. Aquí se da de alta a los otros equipos: API keys y webhooks. */
@Tag(name = "Admin · Integraciones")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
class AdminIntegracionesController {

    private final GestionarClientesApiUseCase clientes;
    private final GestionarWebhooksUseCase webhooks;

    @Operation(summary = "Sistemas de otros equipos con acceso a la API (nunca devuelve la key)")
    @GetMapping("/clientes-api")
    List<ClienteResponse> listar() {
        return clientes.listar().stream().map(ClienteResponse::de).toList();
    }

    @Operation(summary = "Crea un cliente y su API key. La key se muestra UNA sola vez")
    @PostMapping("/clientes-api")
    @ResponseStatus(HttpStatus.CREATED)
    ClienteCreadoResponse crear(@Valid @RequestBody CrearClienteRequest r) {
        var creado = clientes.crear(r.nombre(), r.descripcion(), r.responsableEmail(), r.scopes(), r.diasVigencia());
        return new ClienteCreadoResponse(ClienteResponse.de(creado.cliente()), creado.apiKey());
    }

    @Operation(summary = "Revoca la API key de inmediato")
    @DeleteMapping("/clientes-api/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revocar(@PathVariable UUID id) {
        clientes.revocar(id);
    }

    @Operation(summary = "Webhooks configurados")
    @GetMapping("/webhooks")
    List<WebhookResponse> listarWebhooks() {
        return webhooks.listarSuscripciones().stream().map(WebhookResponse::de).toList();
    }

    @Operation(summary = "Suscribe un cliente a un evento. El secreto de firma se muestra UNA sola vez")
    @PostMapping("/webhooks")
    @ResponseStatus(HttpStatus.CREATED)
    WebhookCreadoResponse crearWebhook(@Valid @RequestBody CrearWebhookRequest r) {
        var creado = webhooks.crear(r.clienteApiId(), r.tipoEvento(), r.url());
        return new WebhookCreadoResponse(WebhookResponse.de(creado.suscripcion()), creado.secreto());
    }

    @Operation(summary = "Elimina un webhook")
    @DeleteMapping("/webhooks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void eliminarWebhook(@PathVariable UUID id) {
        webhooks.eliminar(id);
    }

    record CrearClienteRequest(
            @NotBlank @Size(max = 80) String nombre,
            @Size(max = 250) String descripcion,
            @Email String responsableEmail,
            @NotEmpty Set<String> scopes,
            @Min(1) @Max(3650) Integer diasVigencia) {
    }

    record ClienteResponse(UUID id, String nombre, String descripcion, String responsableEmail, String prefijoKey,
            Set<String> scopes, boolean activo, Instant expiraEn, Instant ultimoUso) {

        static ClienteResponse de(ClienteApi c) {
            return new ClienteResponse(c.id(), c.nombre(), c.descripcion(), c.responsableEmail(), c.apiKeyPrefijo(),
                    c.scopes(), c.activo(), c.expiraEn(), c.ultimoUsoAt());
        }
    }

    record ClienteCreadoResponse(ClienteResponse cliente, String apiKey) {
    }

    record CrearWebhookRequest(@NotNull UUID clienteApiId, @NotBlank String tipoEvento, @NotBlank @Size(max = 255) String url) {
    }

    record WebhookResponse(UUID id, UUID clienteApiId, String tipoEvento, String url, boolean activa) {

        static WebhookResponse de(SuscripcionWebhook s) {
            return new WebhookResponse(s.id(), s.clienteApiId(), s.tipoEvento(), s.url(), s.activa());
        }
    }

    record WebhookCreadoResponse(WebhookResponse webhook, String secreto) {
    }
}
