package com.selahfinance.categorias.infrastructure.persistence;

import com.selahfinance.categorias.application.port.out.FuenteIngresoRepositoryPort;
import com.selahfinance.categorias.domain.model.FuenteIngreso;
import com.selahfinance.shared.domain.Dinero;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class FuenteIngresoPersistenceAdapter implements FuenteIngresoRepositoryPort {

    private final FuenteIngresoJpaRepository fuentes;

    @Override
    public FuenteIngreso guardar(FuenteIngreso f) {
        var e = fuentes.findById(f.id()).orElseGet(FuenteIngresoJpaEntity::new);
        e.setId(f.id());
        e.setHogarId(f.hogarId());
        e.setNombre(f.nombre());
        e.setMontoEstimadoMensual(f.montoEstimadoMensual().valor());
        e.setActiva(f.activa());
        return aDominio(fuentes.save(e));
    }

    @Override
    public Optional<FuenteIngreso> porId(UUID hogarId, UUID fuenteId) {
        return fuentes.findByIdAndHogarId(fuenteId, hogarId).map(FuenteIngresoPersistenceAdapter::aDominio);
    }

    @Override
    public List<FuenteIngreso> activasDelHogar(UUID hogarId) {
        return fuentes.findByHogarIdAndActivaTrueOrderByNombreAsc(hogarId).stream()
                .map(FuenteIngresoPersistenceAdapter::aDominio).toList();
    }

    @Override
    public boolean existeNombre(UUID hogarId, String nombre, UUID excluirId) {
        return fuentes.existeNombre(hogarId, nombre, excluirId);
    }

    private static FuenteIngreso aDominio(FuenteIngresoJpaEntity e) {
        return new FuenteIngreso(e.getId(), e.getHogarId(), e.getNombre(), Dinero.de(e.getMontoEstimadoMensual()),
                e.isActiva());
    }
}
