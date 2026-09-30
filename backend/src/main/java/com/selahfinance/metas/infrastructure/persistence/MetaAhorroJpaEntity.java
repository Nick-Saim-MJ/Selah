package com.selahfinance.metas.infrastructure.persistence;

import com.selahfinance.shared.infrastructure.persistence.EntidadConIdAsignado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meta_ahorro")
@Getter
@Setter
@NoArgsConstructor
public class MetaAhorroJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(name = "hogar_id", nullable = false)
    private UUID hogarId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String proposito;

    @Column(nullable = false)
    private String tipo;

    @Column(name = "monto_objetivo", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoObjetivo;

    @Column(name = "fecha_objetivo")
    private LocalDate fechaObjetivo;

    @Column(name = "es_principal", nullable = false)
    private boolean esPrincipal;

    @Column(nullable = false)
    private String estado;
}
