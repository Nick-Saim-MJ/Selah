package com.selahfinance.integraciones.application.usecase;

import com.selahfinance.integraciones.application.port.in.AutenticarClienteApiUseCase;
import com.selahfinance.integraciones.application.port.out.ClienteApiRepositoryPort;
import com.selahfinance.integraciones.domain.model.ApiKey;
import java.time.Clock;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class AutenticarClienteApiService implements AutenticarClienteApiUseCase {

    private final ClienteApiRepositoryPort clientes;
    private final Clock clock;

    @Override
    @Transactional
    public Optional<ClienteAutenticado> autenticar(String apiKey) {
        var ahora = clock.instant();
        return ApiKey.prefijoDe(apiKey)
                .flatMap(clientes::porPrefijo)
                .filter(c -> c.vigente(ahora))
                .filter(c -> ApiKey.coincide(apiKey, c.apiKeyHash()))
                .map(c -> {
                    clientes.registrarUso(c.id(), ahora);
                    return new ClienteAutenticado(c.id(), c.nombre(), c.scopes());
                });
    }
}
