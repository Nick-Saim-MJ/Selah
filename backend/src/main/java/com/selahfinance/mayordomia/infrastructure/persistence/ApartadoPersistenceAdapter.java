package com.selahfinance.mayordomia.infrastructure.persistence;

import com.selahfinance.mayordomia.application.port.out.ApartadoRepositoryPort;
import com.selahfinance.mayordomia.domain.model.ApartadoMayordomia;
import com.selahfinance.mayordomia.domain.model.EstadoApartado;
import com.selahfinance.mayordomia.domain.model.TipoApartado;
import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.Periodo;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ApartadoPersistenceAdapter implements ApartadoRepositoryPort {

    private final ApartadoMayordomiaJpaRepository repository;

    @Override
    public void guardarTodos(List<ApartadoMayordomia> apartados) {
        var existentes = repository.findAllById(apartados.stream().map(ApartadoMayordomia::id).toList()).stream()
                .collect(Collectors.toMap(ApartadoMayordomiaJpaEntity::getId, Function.identity()));
        var entidades = apartados.stream()
                .map(a -> copiar(a, existentes.getOrDefault(a.id(), new ApartadoMayordomiaJpaEntity())))
                .toList();
        repository.saveAll(entidades);
    }

    @Override
    public List<ApartadoMayordomia> porHogarYPeriodo(UUID hogarId, Periodo periodo) {
        return repository.findByHogarIdAndPeriodo(hogarId, periodo.inicio()).stream().map(this::aDominio).toList();
    }

    @Override
    public List<ApartadoMayordomia> pendientes(UUID hogarId, Periodo periodo, TipoApartado tipo) {
        return repository.findByHogarIdAndPeriodoAndTipoAndEstado(hogarId, periodo.inicio(), tipo.name(),
                EstadoApartado.PENDIENTE.name()).stream().map(this::aDominio).toList();
    }

    @Override
    public List<ApartadoMayordomia> porMovimientoIngreso(UUID movimientoIngresoId) {
        return repository.findByMovimientoIngresoId(movimientoIngresoId).stream().map(this::aDominio).toList();
    }

    private ApartadoMayordomiaJpaEntity copiar(ApartadoMayordomia a, ApartadoMayordomiaJpaEntity e) {
        e.setId(a.id());
        e.setHogarId(a.hogarId());
        e.setMovimientoIngresoId(a.movimientoIngresoId());
        e.setTipo(a.tipo().name());
        e.setPorcentaje(a.porcentaje());
        e.setBaseCalculo(a.baseCalculo().valor());
        e.setMonto(a.monto().valor());
        e.setPeriodo(a.periodo().inicio());
        e.setEstado(a.estado().name());
        e.setMovimientoEntregaId(a.movimientoEntregaId());
        e.setEntregadoAt(a.entregadoEn());
        return e;
    }

    private ApartadoMayordomia aDominio(ApartadoMayordomiaJpaEntity e) {
        return new ApartadoMayordomia(e.getId(), e.getHogarId(), e.getMovimientoIngresoId(),
                TipoApartado.valueOf(e.getTipo()), e.getPorcentaje(), Dinero.de(e.getBaseCalculo()),
                Dinero.de(e.getMonto()), new Periodo(YearMonth.from(e.getPeriodo())),
                EstadoApartado.valueOf(e.getEstado()), e.getMovimientoEntregaId(), e.getEntregadoAt());
    }
}
