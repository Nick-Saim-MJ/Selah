package com.selahfinance.reportes.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ReporteMayordomiaJpaRepository extends JpaRepository<ReporteMayordomiaJpaEntity, UUID> {

    Optional<ReporteMayordomiaJpaEntity> findByHogarIdAndUsuarioIdAndTipoPeriodoAndPeriodoInicio(UUID hogarId,
            UUID usuarioId, String tipoPeriodo, LocalDate periodoInicio);
}
