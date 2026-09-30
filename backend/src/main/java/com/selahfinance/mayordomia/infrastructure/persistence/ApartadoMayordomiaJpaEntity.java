package com.selahfinance.mayordomia.infrastructure.persistence;

import com.selahfinance.shared.infrastructure.persistence.EntidadConIdAsignado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "apartado_mayordomia")
@Getter
@Setter
@NoArgsConstructor
public class ApartadoMayordomiaJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(name = "hogar_id", nullable = false)
    private UUID hogarId;

    @Column(name = "movimiento_ingreso_id", nullable = false)
    private UUID movimientoIngresoId;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "base_calculo", nullable = false, precision = 14, scale = 2)
    private BigDecimal baseCalculo;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate periodo;

    @Column(nullable = false)
    private String estado;

    @Column(name = "movimiento_entrega_id")
    private UUID movimientoEntregaId;

    @Column(name = "entregado_at")
    private Instant entregadoAt;
}
