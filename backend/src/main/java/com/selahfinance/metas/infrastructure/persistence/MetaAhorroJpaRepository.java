package com.selahfinance.metas.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface MetaAhorroJpaRepository extends JpaRepository<MetaAhorroJpaEntity, UUID> {

    Optional<MetaAhorroJpaEntity> findByIdAndHogarId(UUID id, UUID hogarId);

    List<MetaAhorroJpaEntity> findByHogarIdAndEstadoNot(UUID hogarId, String estado);

    Optional<MetaAhorroJpaEntity> findFirstByHogarIdAndEsPrincipalTrueAndEstado(UUID hogarId, String estado);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update MetaAhorroJpaEntity m set m.esPrincipal = false where m.hogarId = :hogarId and m.esPrincipal = true")
    void quitarPrincipal(@Param("hogarId") UUID hogarId);
}
