package com.selahfinance.integraciones.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ClienteApiJpaRepository extends JpaRepository<ClienteApiJpaEntity, UUID> {

    Optional<ClienteApiJpaEntity> findByApiKeyPrefijo(String apiKeyPrefijo);

    @Modifying
    @Query(value = "update cliente_api set ultimo_uso_at = :momento where id = :id", nativeQuery = true)
    void registrarUso(@Param("id") UUID id, @Param("momento") Instant momento);
}
