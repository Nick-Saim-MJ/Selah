package com.selahfinance.shared.infrastructure.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "evento_outbox")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEventoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(nullable = false)
    private String agregado;

    @Column(name = "agregado_id", nullable = false)
    private UUID agregadoId;

    @Column(name = "hogar_id")
    private UUID hogarId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private short intentos;

    @Column(name = "ultimo_error")
    private String ultimoError;

    @Column(name = "ocurrido_at", nullable = false)
    private Instant ocurridoAt;

    @Column(name = "publicado_at")
    private Instant publicadoAt;
}
