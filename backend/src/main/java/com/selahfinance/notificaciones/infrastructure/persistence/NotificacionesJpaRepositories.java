package com.selahfinance.notificaciones.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface NotificacionJpaRepository extends JpaRepository<NotificacionJpaEntity, UUID> {

    boolean existsByUsuarioIdAndClaveDedupe(UUID usuarioId, String claveDedupe);

    Optional<NotificacionJpaEntity> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    Page<NotificacionJpaEntity> findByUsuarioIdAndProgramadaParaLessThanEqualOrderByProgramadaParaDesc(UUID usuarioId,
            Instant ahora, Pageable pageable);

    long countByUsuarioIdAndLeidaAtIsNullAndProgramadaParaLessThanEqual(UUID usuarioId, Instant ahora);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update NotificacionJpaEntity n set n.leidaAt = :ahora
            where n.usuarioId = :usuarioId and n.leidaAt is null and n.programadaPara <= :ahora
            """)
    void marcarTodasLeidas(@Param("usuarioId") UUID usuarioId, @Param("ahora") Instant ahora);
}

interface PreferenciaNotificacionJpaRepository extends JpaRepository<PreferenciaNotificacionJpaEntity, UUID> {
}
