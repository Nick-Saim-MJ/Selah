package com.selahfinance.integraciones.application.port.out;

import com.selahfinance.integraciones.domain.model.ClienteApi;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteApiRepositoryPort {

    ClienteApi guardar(ClienteApi cliente);

    Optional<ClienteApi> porId(UUID id);

    Optional<ClienteApi> porPrefijo(String prefijo);

    List<ClienteApi> todos();

    void registrarUso(UUID clienteId, Instant momento);
}
