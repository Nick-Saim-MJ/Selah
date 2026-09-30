package com.selahfinance.mayordomia.application.usecase;

import com.selahfinance.mayordomia.application.port.in.EntregarMayordomiaUseCase;
import com.selahfinance.mayordomia.application.port.out.ApartadoRepositoryPort;
import com.selahfinance.mayordomia.application.port.out.RegistroEntregaPort;
import com.selahfinance.mayordomia.domain.model.ApartadoMayordomia;
import com.selahfinance.mayordomia.domain.model.TipoApartado;
import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class EntregarMayordomiaService implements EntregarMayordomiaUseCase {

    private final ApartadoRepositoryPort apartados;
    private final RegistroEntregaPort registroEntrega;
    private final Clock clock;

    @Override
    @Transactional
    public Resultado entregar(Comando c) {
        var pendientes = apartados.pendientes(c.hogarId(), c.periodo(), c.tipo());
        if (pendientes.isEmpty()) {
            throw new DomainException("NADA_PENDIENTE", "No hay " + c.tipo().name().toLowerCase()
                    + " pendiente en " + c.periodo());
        }
        Dinero total = pendientes.stream().map(ApartadoMayordomia::monto).reduce(Dinero.CERO, Dinero::sumar);
        String descripcion = (c.tipo() == TipoApartado.DIEZMO ? "Diezmo " : "Ofrenda ") + c.periodo();

        var movimientoId = registroEntrega.registrarEntrega(
                c.hogarId(), c.usuarioId(), c.tipo(), total, c.fechaEntrega(), descripcion);

        var ahora = clock.instant();
        pendientes.forEach(a -> a.entregar(movimientoId, ahora));
        apartados.guardarTodos(pendientes);
        return new Resultado(movimientoId, total, pendientes.size());
    }
}
