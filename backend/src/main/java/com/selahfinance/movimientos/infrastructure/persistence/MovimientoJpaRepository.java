package com.selahfinance.movimientos.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface MovimientoJpaRepository extends JpaRepository<MovimientoJpaEntity, UUID>,
        JpaSpecificationExecutor<MovimientoJpaEntity> {

    Optional<MovimientoJpaEntity> findByIdAndHogarIdAndDeletedAtIsNull(UUID id, UUID hogarId);

    Optional<MovimientoJpaEntity> findByHogarIdAndOrigenAndReferenciaExterna(UUID hogarId, String origen,
            String referenciaExterna);
}
