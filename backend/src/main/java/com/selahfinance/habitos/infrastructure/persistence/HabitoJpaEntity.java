package com.selahfinance.habitos.infrastructure.persistence;

import com.selahfinance.shared.infrastructure.persistence.EntidadConIdAsignado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "habito_mayordomia")
@Getter
@Setter
@NoArgsConstructor
public class HabitoJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(name = "hogar_id")
    private UUID hogarId;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String dimension;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private String unidad;

    @Column(name = "frecuencia_meta", nullable = false)
    private String frecuenciaMeta;

    @Column(name = "meta_valor", nullable = false, precision = 8, scale = 2)
    private BigDecimal metaValor;

    @Column(name = "referencia_biblica")
    private String referenciaBiblica;

    @Column(nullable = false)
    private boolean activo;

    @Column(nullable = false)
    private short orden;
}
