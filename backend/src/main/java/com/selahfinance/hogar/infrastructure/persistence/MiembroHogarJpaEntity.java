package com.selahfinance.hogar.infrastructure.persistence;

import com.selahfinance.shared.infrastructure.persistence.EntidadConIdAsignado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "miembro_hogar")
@Getter
@Setter
@NoArgsConstructor
public class MiembroHogarJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(name = "hogar_id", nullable = false)
    private UUID hogarId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(nullable = false)
    private String nombre;

    private String parentesco;

    @Column(nullable = false)
    private String rol;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
