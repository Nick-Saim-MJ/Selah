package com.selahfinance.categorias.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface CategoriaJpaRepository extends JpaRepository<CategoriaJpaEntity, UUID> {

    Optional<CategoriaJpaEntity> findByIdAndHogarId(UUID id, UUID hogarId);

    List<CategoriaJpaEntity> findByHogarIdAndActivaTrueOrderByOrdenAscNombreAsc(UUID hogarId);

    List<CategoriaJpaEntity> findByHogarIdAndTipoAndActivaTrueOrderByOrdenAscNombreAsc(UUID hogarId, String tipo);

    long countByHogarId(UUID hogarId);

    @Query("""
            select count(c) > 0 from CategoriaJpaEntity c
            where c.hogarId = :hogarId and c.tipo = :tipo and lower(c.nombre) = lower(:nombre)
              and (:excluirId is null or c.id <> :excluirId)
            """)
    boolean existeNombre(@Param("hogarId") UUID hogarId, @Param("tipo") String tipo, @Param("nombre") String nombre,
            @Param("excluirId") UUID excluirId);
}

interface FuenteIngresoJpaRepository extends JpaRepository<FuenteIngresoJpaEntity, UUID> {

    Optional<FuenteIngresoJpaEntity> findByIdAndHogarId(UUID id, UUID hogarId);

    List<FuenteIngresoJpaEntity> findByHogarIdAndActivaTrueOrderByNombreAsc(UUID hogarId);

    @Query("""
            select count(f) > 0 from FuenteIngresoJpaEntity f
            where f.hogarId = :hogarId and lower(f.nombre) = lower(:nombre)
              and (:excluirId is null or f.id <> :excluirId)
            """)
    boolean existeNombre(@Param("hogarId") UUID hogarId, @Param("nombre") String nombre,
            @Param("excluirId") UUID excluirId);
}
