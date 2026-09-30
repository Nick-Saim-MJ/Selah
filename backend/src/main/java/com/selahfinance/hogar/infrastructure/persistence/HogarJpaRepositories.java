package com.selahfinance.hogar.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface HogarJpaRepository extends JpaRepository<HogarJpaEntity, UUID> {
}

interface MiembroHogarJpaRepository extends JpaRepository<MiembroHogarJpaEntity, UUID> {

    @Query("""
            select m.hogarId from MiembroHogarJpaEntity m
            where m.usuarioId = :usuarioId and m.activo = true
            order by m.createdAt asc
            """)
    List<UUID> hogaresDeUsuario(@Param("usuarioId") UUID usuarioId, Limit limit);
}

interface ConfiguracionMayordomiaJpaRepository extends JpaRepository<ConfiguracionMayordomiaJpaEntity, UUID> {
}
