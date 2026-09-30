package com.selahfinance.deudas.infrastructure.persistence;

import com.selahfinance.deudas.application.port.out.DeudaRepositoryPort;
import com.selahfinance.deudas.domain.model.DetallePago;
import com.selahfinance.deudas.domain.model.Deuda;
import com.selahfinance.deudas.domain.model.EstadoDeuda;
import com.selahfinance.shared.domain.Dinero;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DeudaPersistenceAdapter implements DeudaRepositoryPort {

    private final DeudaJpaRepository deudas;
    private final PagoDeudaJpaRepository pagos;

    @Override
    public Deuda guardar(Deuda d) {
        var e = deudas.findById(d.id()).orElseGet(DeudaJpaEntity::new);
        e.setId(d.id());
        e.setHogarId(d.hogarId());
        e.setNombre(d.nombre());
        e.setAcreedor(d.acreedor());
        e.setMontoOriginal(d.montoOriginal().valor());
        e.setSaldoActual(d.saldoActual().valor());
        e.setTasaAnual(d.tasaAnual());
        e.setPlazoMeses((short) d.plazoMeses());
        e.setCuotaMensual(d.cuotaMensual().valor());
        e.setFechaInicio(d.fechaInicio());
        e.setDiaPago(d.diaPago() == null ? null : d.diaPago().shortValue());
        e.setEstado(d.estado().name());
        return aDominio(deudas.saveAndFlush(e));
    }

    @Override
    public Optional<Deuda> porId(UUID hogarId, UUID deudaId) {
        return deudas.findByIdAndHogarId(deudaId, hogarId).map(DeudaPersistenceAdapter::aDominio);
    }

    @Override
    public Optional<Deuda> porIdSinHogar(UUID deudaId) {
        return deudas.findById(deudaId).map(DeudaPersistenceAdapter::aDominio);
    }

    @Override
    public List<Deuda> activasDelHogar(UUID hogarId) {
        return deudas.findByHogarIdAndEstadoOrderByNombreAsc(hogarId, EstadoDeuda.ACTIVA.name()).stream()
                .map(DeudaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public void guardarPago(UUID movimientoId, UUID deudaId, DetallePago detalle) {
        var e = new PagoDeudaJpaEntity();
        e.setMovimientoId(movimientoId);
        e.setDeudaId(deudaId);
        e.setMontoInteres(detalle.interes().valor());
        e.setMontoCapital(detalle.capital().valor());
        pagos.save(e);
    }

    @Override
    public Optional<PagoRegistrado> pagoDeMovimiento(UUID movimientoId) {
        return pagos.findById(movimientoId).map(e -> new PagoRegistrado(e.getDeudaId(),
                new DetallePago(Dinero.de(e.getMontoInteres()), Dinero.de(e.getMontoCapital())),
                e.getRevertidoAt() != null));
    }

    @Override
    public void marcarPagoRevertido(UUID movimientoId, Instant momento) {
        pagos.findById(movimientoId).ifPresent(e -> {
            e.setRevertidoAt(momento);
            pagos.save(e);
        });
    }

    private static Deuda aDominio(DeudaJpaEntity e) {
        return new Deuda(e.getId(), e.getHogarId(), e.getNombre(), e.getAcreedor(), Dinero.de(e.getMontoOriginal()),
                Dinero.de(e.getSaldoActual()), e.getTasaAnual(), e.getPlazoMeses(), Dinero.de(e.getCuotaMensual()),
                e.getFechaInicio(), e.getDiaPago() == null ? null : e.getDiaPago().intValue(),
                EstadoDeuda.valueOf(e.getEstado()));
    }
}
