package com.selahfinance.identidad.infrastructure.persistence;

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
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "citext")
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String nombres;

    private String apellidos;

    @Column(name = "iglesia_id")
    private UUID iglesiaId;

    @Column(nullable = false)
    private String rol;

    @Column(nullable = false)
    private String estado;

    @Column(name = "debe_cambiar_password", nullable = false)
    private boolean debeCambiarPassword;

    @Column(name = "onboarding_completado", nullable = false)
    private boolean onboardingCompletado;

    @Column(name = "comparte_reporte_desde")
    private Instant comparteReporteDesde;
}
