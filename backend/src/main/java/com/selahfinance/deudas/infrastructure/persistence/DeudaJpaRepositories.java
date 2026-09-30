package com.selahfinance.deudas.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface DeudaJpaRepository extends JpaRepository<DeudaJpaEntity, UUID> {

    Optional<DeudaJpaEntity> findByIdAndHogarId(UUID id, UUID hogarId);

    List<DeudaJpaEntity> findByHogarIdAndEstadoOrderByNombreAsc(UUID hogarId, String estado);
}

interface PagoDeudaJpaRepository extends JpaRepository<PagoDeudaJpaEntity, UUID> {
}
