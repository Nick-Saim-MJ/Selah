package com.selahfinance.categorias.infrastructure.persistence;

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
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
public class CategoriaJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(name = "hogar_id", nullable = false)
    private UUID hogarId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String tipo;

    private String color;

    private String icono;

    @Column(name = "presupuesto_mensual", nullable = false, precision = 14, scale = 2)
    private BigDecimal presupuestoMensual;

    @Column(nullable = false)
    private short orden;

    @Column(nullable = false)
    private boolean activa;
}
