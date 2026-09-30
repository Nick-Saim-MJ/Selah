package com.selahfinance.mayordomia.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ApartadoMayordomiaJpaRepository extends JpaRepository<ApartadoMayordomiaJpaEntity, UUID> {

    List<ApartadoMayordomiaJpaEntity> findByHogarIdAndPeriodo(UUID hogarId, LocalDate periodo);

    List<ApartadoMayordomiaJpaEntity> findByHogarIdAndPeriodoAndTipoAndEstado(UUID hogarId, LocalDate periodo,
            String tipo, String estado);

    List<ApartadoMayordomiaJpaEntity> findByMovimientoIngresoId(UUID movimientoIngresoId);
}
