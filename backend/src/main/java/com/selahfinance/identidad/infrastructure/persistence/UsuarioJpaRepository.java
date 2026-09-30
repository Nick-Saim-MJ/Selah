package com.selahfinance.identidad.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, UUID>, JpaSpecificationExecutor<UsuarioJpaEntity> {

    boolean existsByEmail(String email);

    Optional<UsuarioJpaEntity> findByEmail(String email);

    @Query("select u.rol, u.estado, count(u) from UsuarioJpaEntity u group by u.rol, u.estado")
    List<Object[]> contarPorRolYEstado();
}
