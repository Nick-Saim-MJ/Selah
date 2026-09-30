package com.selahfinance.integraciones.application.usecase;

import com.selahfinance.integraciones.application.port.in.GestionarClientesApiUseCase;
import com.selahfinance.integraciones.application.port.in.GestionarWebhooksUseCase;
import com.selahfinance.integraciones.application.port.out.ClienteApiRepositoryPort;
import com.selahfinance.integraciones.application.port.out.WebhooksPorts.CifradorPort;
import com.selahfinance.integraciones.application.port.out.WebhooksPorts.SuscripcionRepositoryPort;
import com.selahfinance.integraciones.domain.model.ApiKey;
import com.selahfinance.integraciones.domain.model.ClienteApi;
import com.selahfinance.integraciones.domain.model.SuscripcionWebhook;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import com.selahfinance.shared.domain.DomainException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Alta y baja de API keys y webhooks (solo ADMIN, verificado en la cadena de seguridad). */
@Service
@RequiredArgsConstructor
class AdministracionIntegracionesService implements GestionarClientesApiUseCase, GestionarWebhooksUseCase {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ClienteApiRepositoryPort clientes;
    private final SuscripcionRepositoryPort suscripciones;
    private final CifradorPort cifrador;
    private final Clock clock;

    // ---------- API keys

    @Override
    @Transactional
    public ClienteCreado crear(String nombre, String descripcion, String responsableEmail, Set<String> scopes,
            Integer diasVigencia) {
        if (scopes == null || scopes.isEmpty()) {
            throw new DomainException("SCOPES_REQUERIDOS", "Indica al menos un permiso (scope)");
        }
        var key = ApiKey.generar();
        Instant expira = diasVigencia == null ? null : clock.instant().plus(diasVigencia, ChronoUnit.DAYS);
        var cliente = clientes.guardar(ClienteApi.nuevo(nombre, descripcion, responsableEmail, scopes, expira, key));
        return new ClienteCreado(cliente, key.valorPlano());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteApi> listar() {
        return clientes.todos();
    }

    @Override
    @Transactional
    public void revocar(UUID clienteId) {
        var cliente = clientes.porId(clienteId).orElseThrow(() -> new RecursoNoEncontradoException("Cliente API", clienteId));
        clientes.guardar(cliente.revocar());
    }

    // ---------- Webhooks

    @Override
    @Transactional
    public WebhookCreado crear(UUID clienteApiId, String tipoEvento, String url) {
        clientes.porId(clienteApiId).orElseThrow(() -> new RecursoNoEncontradoException("Cliente API", clienteApiId));
        var suscripcion = SuscripcionWebhook.nueva(clienteApiId, tipoEvento, url);
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String secreto = "whsec_" + HexFormat.of().formatHex(bytes);
        suscripciones.guardar(suscripcion, cifrador.cifrar(secreto), clock.instant());
        return new WebhookCreado(suscripcion, secreto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuscripcionWebhook> listarSuscripciones() {
        return suscripciones.todas();
    }

    @Override
    @Transactional
    public void eliminar(UUID suscripcionId) {
        suscripciones.porId(suscripcionId).orElseThrow(() -> new RecursoNoEncontradoException("Webhook", suscripcionId));
        suscripciones.eliminar(suscripcionId);
    }
}
