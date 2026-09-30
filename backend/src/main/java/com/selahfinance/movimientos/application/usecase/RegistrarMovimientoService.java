package com.selahfinance.movimientos.application.usecase;

import com.selahfinance.movimientos.application.port.in.RegistrarMovimientoUseCase;
import com.selahfinance.movimientos.application.port.out.MovimientoRepositoryPort;
import com.selahfinance.movimientos.application.port.out.ReferenciasHogarPort;
import com.selahfinance.movimientos.domain.model.DatosMovimiento;
import com.selahfinance.movimientos.domain.model.Movimiento;
import com.selahfinance.movimientos.domain.model.OrigenMovimiento;
import com.selahfinance.shared.application.port.out.EventPublisherPort;
import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.time.Clock;
import java.util.UUID;
import java.util.function.BiPredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class RegistrarMovimientoService implements RegistrarMovimientoUseCase {

    private final MovimientoRepositoryPort movimientos;
    private final ReferenciasHogarPort referencias;
    private final EventPublisherPort eventos;
    private final Clock clock;

    @Override
    @Transactional
    public Movimiento registrar(Comando c) {
        var origen = c.origen() == null ? OrigenMovimiento.APP : c.origen();
        if (origen != OrigenMovimiento.APP && c.referenciaExterna() != null) {
            var existente = movimientos.porReferenciaExterna(c.hogarId(), origen, c.referenciaExterna());
            if (existente.isPresent()) {
                return existente.get();
            }
        }

        verificar(c.hogarId(), c.categoriaId(), referencias::categoriaPertenece, "Categoría");
        verificar(c.hogarId(), c.fuenteIngresoId(), referencias::fuenteIngresoPertenece, "Fuente de ingreso");
        verificar(c.hogarId(), c.metaId(), referencias::metaPertenece, "Meta");
        verificar(c.hogarId(), c.deudaId(), referencias::deudaPertenece, "Deuda");
        verificar(c.hogarId(), c.destinoOfrendaId(), referencias::destinoOfrendaDisponible, "Destino de ofrenda");

        var movimiento = Movimiento.registrar(new DatosMovimiento(
                c.hogarId(), c.usuarioId(), c.tipo(),
                c.monto() == null ? null : Dinero.de(c.monto()),
                c.fecha(), c.descripcion(), c.nota(),
                c.categoriaId(), c.fuenteIngresoId(), c.metaId(), c.deudaId(), c.destinoOfrendaId(),
                c.esPresupuestado() == null || c.esPresupuestado(),
                origen, c.referenciaExterna()));

        var guardado = movimientos.guardar(movimiento);
        // Síncrono y en la misma transacción: mayordomía genera los apartados de diezmo/ofrenda
        // y el outbox registra el evento para los sistemas externos.
        eventos.publicar(guardado.eventoRegistrado(clock.instant()));
        return guardado;
    }

    private static void verificar(UUID hogarId, UUID id, BiPredicate<UUID, UUID> pertenece, String nombre) {
        if (id != null && !pertenece.test(hogarId, id)) {
            throw new DomainException("REFERENCIA_INVALIDA", nombre + " no pertenece a este hogar");
        }
    }
}
