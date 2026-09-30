package com.selahfinance.integraciones.infrastructure.persistence;

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
@Table(name = "cliente_api")
@Getter
@Setter
@NoArgsConstructor
public class ClienteApiJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(name = "responsable_email", columnDefinition = "citext")
    private String responsableEmail;

    @Column(name = "api_key_prefijo", nullable = false)
    private String apiKeyPrefijo;

    @Column(name = "api_key_hash", nullable = false)
    private String apiKeyHash;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private String[] scopes;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "expira_at")
    private Instant expiraAt;

    /** Lo actualiza {@code registrarUso} con SQL directo; aquí solo se lee. */
    @Column(name = "ultimo_uso_at", insertable = false, updatable = false)
    private Instant ultimoUsoAt;
}
