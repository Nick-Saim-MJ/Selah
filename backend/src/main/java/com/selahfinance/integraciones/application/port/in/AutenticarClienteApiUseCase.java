package com.selahfinance.integraciones.application.port.in;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AutenticarClienteApiUseCase {

    Optional<ClienteAutenticado> autenticar(String apiKey);

    record ClienteAutenticado(UUID id, String nombre, Set<String> scopes) {
    }
}
