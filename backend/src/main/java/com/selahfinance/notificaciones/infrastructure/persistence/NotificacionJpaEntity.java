package com.selahfinance.notificaciones.infrastructure.persistence;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
public class NotificacionJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private String titulo;

    private String cuerpo;

    @JdbcTypeCode(SqlTypes.JSON)
    private String datos;

    @Column(name = "programada_para", nullable = false)
    private Instant programadaPara;

    @Column(name = "leida_at")
    private Instant leidaAt;

    @Column(name = "created_at", nullable = false)
    private Instant creadaAt;

    @Column(name = "clave_dedupe")
    private String claveDedupe;
}
