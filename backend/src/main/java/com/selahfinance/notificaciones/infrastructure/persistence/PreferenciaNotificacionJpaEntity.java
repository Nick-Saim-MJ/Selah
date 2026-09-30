package com.selahfinance.notificaciones.infrastructure.persistence;

import com.selahfinance.shared.infrastructure.persistence.EntidadConIdAsignado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Solo se mapean las columnas editables; hora_resumen, push_token y updated_at los maneja la BD. */
@Entity
@Table(name = "preferencia_notificacion")
@Getter
@Setter
@NoArgsConstructor
public class PreferenciaNotificacionJpaEntity extends EntidadConIdAsignado {

    @Id
    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "recordatorio_diezmo", nullable = false)
    private boolean recordatorioDiezmo;

    @Column(name = "recordatorio_deudas", nullable = false)
    private boolean recordatorioDeudas;

    @Column(name = "resumen_semanal", nullable = false)
    private boolean resumenSemanal;

    @Column(name = "reflexion_sabado", nullable = false)
    private boolean reflexionSabado;

    @Override
    public UUID getId() {
        return usuarioId;
    }
}
