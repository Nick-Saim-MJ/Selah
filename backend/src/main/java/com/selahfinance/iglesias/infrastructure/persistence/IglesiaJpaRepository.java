package com.selahfinance.iglesias.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface IglesiaJpaRepository extends JpaRepository<IglesiaJpaEntity, UUID> {

    List<IglesiaJpaEntity> findByActivaTrueOrderByNombreAsc();

    List<IglesiaJpaEntity> findAllByOrderByNombreAsc();

    @Query("""
            select count(i) > 0 from IglesiaJpaEntity i
            where lower(i.nombre) = lower(:nombre)
              and lower(coalesce(i.ciudad, '')) = lower(coalesce(:ciudad, ''))
              and (:excluirId is null or i.id <> :excluirId)
            """)
    boolean existeNombre(@Param("nombre") String nombre, @Param("ciudad") String ciudad,
            @Param("excluirId") UUID excluirId);
}
