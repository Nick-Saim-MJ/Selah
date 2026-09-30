package com.selahfinance.reportes.infrastructure.persistence;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "reporte_mayordomia")
@Getter
@Setter
@NoArgsConstructor
public class ReporteMayordomiaJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(name = "hogar_id", nullable = false)
    private UUID hogarId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "tipo_periodo", nullable = false)
    private String tipoPeriodo;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @Column(name = "puntaje_tiempo", precision = 5, scale = 2)
    private BigDecimal puntajeTiempo;

    @Column(name = "puntaje_talento", precision = 5, scale = 2)
    private BigDecimal puntajeTalento;

    @Column(name = "puntaje_tesoro", precision = 5, scale = 2)
    private BigDecimal puntajeTesoro;

    @Column(name = "puntaje_templo", precision = 5, scale = 2)
    private BigDecimal puntajeTemplo;

    @Column(name = "puntaje_global", precision = 5, scale = 2)
    private BigDecimal puntajeGlobal;

    private String semaforo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private String detalle;

    @Column(name = "version_algoritmo", nullable = false)
    private String versionAlgoritmo;

    @Column(name = "generado_at", nullable = false)
    private Instant generadoAt;
}
