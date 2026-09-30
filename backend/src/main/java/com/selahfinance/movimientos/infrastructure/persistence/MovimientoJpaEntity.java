package com.selahfinance.movimientos.infrastructure.persistence;

import com.selahfinance.shared.infrastructure.persistence.EntidadConIdAsignado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movimiento")
@Getter
@Setter
@NoArgsConstructor
public class MovimientoJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(name = "hogar_id", nullable = false)
    private UUID hogarId;

    @Column(name = "registrado_por", nullable = false)
    private UUID registradoPor;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate fecha;

    private String descripcion;

    private String nota;

    @Column(name = "categoria_id")
    private UUID categoriaId;

    @Column(name = "fuente_ingreso_id")
    private UUID fuenteIngresoId;

    @Column(name = "meta_id")
    private UUID metaId;

    @Column(name = "deuda_id")
    private UUID deudaId;

    @Column(name = "destino_ofrenda_id")
    private UUID destinoOfrendaId;

    @Column(name = "es_presupuestado", nullable = false)
    private boolean esPresupuestado;

    @Column(nullable = false)
    private String origen;

    @Column(name = "referencia_externa")
    private String referenciaExterna;

    @Version
    private int version;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
