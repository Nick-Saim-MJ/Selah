package com.selahfinance.deudas.infrastructure.persistence;

import com.selahfinance.shared.infrastructure.persistence.EntidadConIdAsignado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pago_deuda")
@Getter
@Setter
@NoArgsConstructor
public class PagoDeudaJpaEntity extends EntidadConIdAsignado {

    @Id
    @Column(name = "movimiento_id")
    private UUID movimientoId;

    @Column(name = "deuda_id", nullable = false)
    private UUID deudaId;

    @Column(name = "monto_interes", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoInteres;

    @Column(name = "monto_capital", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoCapital;

    @Column(name = "revertido_at")
    private Instant revertidoAt;

    @Override
    public UUID getId() {
        return movimientoId;
    }
}
