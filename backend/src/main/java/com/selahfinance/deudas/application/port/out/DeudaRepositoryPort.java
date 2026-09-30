package com.selahfinance.deudas.application.port.out;

import com.selahfinance.deudas.domain.model.DetallePago;
import com.selahfinance.deudas.domain.model.Deuda;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeudaRepositoryPort {

    Deuda guardar(Deuda deuda);

    Optional<Deuda> porId(UUID hogarId, UUID deudaId);

    Optional<Deuda> porIdSinHogar(UUID deudaId);

    /** Solo ACTIVAS. */
    List<Deuda> activasDelHogar(UUID hogarId);

    void guardarPago(UUID movimientoId, UUID deudaId, DetallePago detalle);

    Optional<PagoRegistrado> pagoDeMovimiento(UUID movimientoId);

    void marcarPagoRevertido(UUID movimientoId, Instant momento);

    record PagoRegistrado(UUID deudaId, DetallePago detalle, boolean revertido) {
    }
}
