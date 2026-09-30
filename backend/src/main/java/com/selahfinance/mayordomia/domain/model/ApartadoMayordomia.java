package com.selahfinance.mayordomia.domain.model;

import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import com.selahfinance.shared.domain.Periodo;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Porción de un ingreso reservada para Dios (primicias, Prov. 3:9).
 * Se descuenta del saldo disponible desde que se aparta, no cuando se entrega.
 */
public final class ApartadoMayordomia {

    private final UUID id;
    private final UUID hogarId;
    private final UUID movimientoIngresoId;
    private final TipoApartado tipo;
    private final BigDecimal porcentaje;
    private final Dinero baseCalculo;
    private final Dinero monto;
    private final Periodo periodo;
    private EstadoApartado estado;
    private UUID movimientoEntregaId;
    private Instant entregadoEn;

    public ApartadoMayordomia(UUID id, UUID hogarId, UUID movimientoIngresoId, TipoApartado tipo, BigDecimal porcentaje,
            Dinero baseCalculo, Dinero monto, Periodo periodo, EstadoApartado estado, UUID movimientoEntregaId,
            Instant entregadoEn) {
        this.id = id;
        this.hogarId = hogarId;
        this.movimientoIngresoId = movimientoIngresoId;
        this.tipo = tipo;
        this.porcentaje = porcentaje;
        this.baseCalculo = baseCalculo;
        this.monto = monto;
        this.periodo = periodo;
        this.estado = estado;
        this.movimientoEntregaId = movimientoEntregaId;
        this.entregadoEn = entregadoEn;
    }

    public static ApartadoMayordomia pendiente(UUID hogarId, UUID movimientoIngresoId, TipoApartado tipo,
            BigDecimal porcentaje, Dinero baseCalculo, Dinero monto, Periodo periodo) {
        return new ApartadoMayordomia(UUID.randomUUID(), hogarId, movimientoIngresoId, tipo, porcentaje, baseCalculo,
                monto, periodo, EstadoApartado.PENDIENTE, null, null);
    }

    public void entregar(UUID movimientoEntregaId, Instant ahora) {
        if (estado != EstadoApartado.PENDIENTE) {
            throw new DomainException("APARTADO_NO_PENDIENTE", "Solo se puede entregar un apartado pendiente");
        }
        this.estado = EstadoApartado.ENTREGADO;
        this.movimientoEntregaId = movimientoEntregaId;
        this.entregadoEn = ahora;
    }

    /** Anula el apartado si aún no se entregó. Lo ya entregado se conserva: fue dado. */
    public boolean anularSiPendiente() {
        if (estado == EstadoApartado.PENDIENTE) {
            estado = EstadoApartado.ANULADO;
            return true;
        }
        return false;
    }

    public UUID id() {
        return id;
    }

    public UUID hogarId() {
        return hogarId;
    }

    public UUID movimientoIngresoId() {
        return movimientoIngresoId;
    }

    public TipoApartado tipo() {
        return tipo;
    }

    public BigDecimal porcentaje() {
        return porcentaje;
    }

    public Dinero baseCalculo() {
        return baseCalculo;
    }

    public Dinero monto() {
        return monto;
    }

    public Periodo periodo() {
        return periodo;
    }

    public EstadoApartado estado() {
        return estado;
    }

    public UUID movimientoEntregaId() {
        return movimientoEntregaId;
    }

    public Instant entregadoEn() {
        return entregadoEn;
    }
}
