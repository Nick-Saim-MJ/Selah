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

@Entity
@Table(name = "configuracion_mayordomia")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionMayordomiaJpaEntity extends EntidadConIdAsignado {

    @Id
    @Column(name = "hogar_id")
    private UUID hogarId;

    @Column(name = "pct_diezmo", nullable = false)
    private BigDecimal pctDiezmo;

    @Column(name = "ofrenda_activa", nullable = false)
    private boolean ofrendaActiva;

    @Column(name = "pct_ofrenda", nullable = false)
    private BigDecimal pctOfrenda;

    @Column(name = "dia_entrega_diezmo")
    private Short diaEntregaDiezmo;

    @Column(name = "modo_sabado_activo", nullable = false)
    private boolean modoSabadoActivo;

    @Override
    public UUID getId() {
        return hogarId;
    }
}
