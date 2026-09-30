package com.selahfinance.hogar.infrastructure.persistence;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "hogar")
@Getter
@Setter
@NoArgsConstructor
public class HogarJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String tipo;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(name = "zona_horaria", nullable = false)
    private String zonaHoraria;

    private BigDecimal latitud;

    private BigDecimal longitud;

    @Column(name = "creado_por", nullable = false)
    private UUID creadoPor;
}
