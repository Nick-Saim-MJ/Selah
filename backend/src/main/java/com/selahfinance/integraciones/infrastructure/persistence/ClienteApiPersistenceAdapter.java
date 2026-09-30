package com.selahfinance.integraciones.infrastructure.persistence;

import com.selahfinance.integraciones.application.port.out.ClienteApiRepositoryPort;
import com.selahfinance.integraciones.domain.model.ClienteApi;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ClienteApiPersistenceAdapter implements ClienteApiRepositoryPort {

    private final ClienteApiJpaRepository repository;

    @Override
    public ClienteApi guardar(ClienteApi c) {
        var e = repository.findById(c.id()).orElseGet(ClienteApiJpaEntity::new);
        e.setId(c.id());
        e.setNombre(c.nombre());
        e.setDescripcion(c.descripcion());
        e.setResponsableEmail(c.responsableEmail());
        e.setApiKeyPrefijo(c.apiKeyPrefijo());
        e.setApiKeyHash(c.apiKeyHash());
        e.setScopes(c.scopes().toArray(String[]::new));
        e.setActivo(c.activo());
        e.setExpiraAt(c.expiraEn());
        return aDominio(repository.saveAndFlush(e));
    }

    @Override
    public Optional<ClienteApi> porId(UUID id) {
        return repository.findById(id).map(ClienteApiPersistenceAdapter::aDominio);
    }

    @Override
    public Optional<ClienteApi> porPrefijo(String prefijo) {
        return repository.findByApiKeyPrefijo(prefijo).map(ClienteApiPersistenceAdapter::aDominio);
    }

    @Override
    public List<ClienteApi> todos() {
        return repository.findAll(Sort.by("nombre")).stream().map(ClienteApiPersistenceAdapter::aDominio).toList();
    }

    @Override
    public void registrarUso(UUID clienteId, Instant momento) {
        repository.registrarUso(clienteId, momento);
    }

    private static ClienteApi aDominio(ClienteApiJpaEntity e) {
        return new ClienteApi(e.getId(), e.getNombre(), e.getDescripcion(), e.getResponsableEmail(),
                e.getApiKeyPrefijo(), e.getApiKeyHash(), e.getScopes() == null ? Set.of() : Set.of(e.getScopes()),
                e.isActivo(), e.getExpiraAt(), e.getUltimoUsoAt());
    }
}
