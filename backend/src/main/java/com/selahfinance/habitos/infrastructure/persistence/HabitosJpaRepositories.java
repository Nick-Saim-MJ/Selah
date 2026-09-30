package com.selahfinance.habitos.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface HabitoJpaRepository extends JpaRepository<HabitoJpaEntity, UUID> {

    @Query("""
            select h from HabitoJpaEntity h
            where h.activo = true and (h.hogarId is null or h.hogarId = :hogarId)
            order by h.dimension, h.orden
            """)
    List<HabitoJpaEntity> activos(@Param("hogarId") UUID hogarId);

    /** Un hábito propio del hogar tiene prioridad sobre el del sistema con el mismo código. */
    @Query("""
            select h from HabitoJpaEntity h
            where h.activo = true and h.codigo = :codigo and (h.hogarId is null or h.hogarId = :hogarId)
            order by case when h.hogarId is null then 1 else 0 end
            """)
    List<HabitoJpaEntity> porCodigo(@Param("hogarId") UUID hogarId, @Param("codigo") String codigo);
}

interface RegistroHabitoJpaRepository extends JpaRepository<RegistroHabitoJpaEntity, UUID> {

    Optional<RegistroHabitoJpaEntity> findByUsuarioIdAndHabitoIdAndFechaAndFuente(UUID usuarioId, UUID habitoId,
            LocalDate fecha, String fuente);

    @Query("""
            select r.habitoId, sum(r.valor) from RegistroHabitoJpaEntity r
            where r.usuarioId = :usuarioId and r.fuente = 'APP' and r.fecha between :desde and :hasta
            group by r.habitoId
            """)
    List<Object[]> sumasPorHabito(@Param("usuarioId") UUID usuarioId, @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    default java.util.Map<UUID, BigDecimal> sumas(UUID usuarioId, LocalDate desde, LocalDate hasta) {
        var mapa = new java.util.HashMap<UUID, BigDecimal>();
        for (Object[] fila : sumasPorHabito(usuarioId, desde, hasta)) {
            mapa.put((UUID) fila[0], (BigDecimal) fila[1]);
        }
        return mapa;
    }
}
