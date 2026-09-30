package com.selahfinance.deudas.infrastructure.persistence;

import com.selahfinance.shared.infrastructure.persistence.EntidadConIdAsignado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "deuda")
@Getter
@Setter
@NoArgsConstructor
public class DeudaJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(name = "hogar_id", nullable = false)
    private UUID hogarId;

    @Column(nullable = false)
    private String nombre;

    private String acreedor;

    @Column(name = "monto_original", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoOriginal;

    @Column(name = "saldo_actual", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoActual;

    @Column(name = "tasa_anual", nullable = false, precision = 7, scale = 4)
    private BigDecimal tasaAnual;

    @Column(name = "plazo_meses", nullable = false)
    private short plazoMeses;

    @Column(name = "cuota_mensual", nullable = false, precision = 14, scale = 2)
    private BigDecimal cuotaMensual;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "dia_pago")
    private Short diaPago;

    @Column(nullable = false)
    private String estado;

    /** Bloqueo optimista: el saldo cambia con cada pago. */
    @Version
    private int version;
}
