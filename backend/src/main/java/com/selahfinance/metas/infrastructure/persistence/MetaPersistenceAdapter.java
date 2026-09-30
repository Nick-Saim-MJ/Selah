package com.selahfinance.metas.infrastructure.persistence;

import com.selahfinance.metas.application.port.out.MetaRepositoryPort;
import com.selahfinance.metas.domain.model.EstadoMeta;
import com.selahfinance.metas.domain.model.MetaAhorro;
import com.selahfinance.metas.domain.model.TipoMeta;
import com.selahfinance.shared.domain.Dinero;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MetaPersistenceAdapter implements MetaRepositoryPort {

    private final MetaAhorroJpaRepository repository;

    @Override
    public MetaAhorro guardar(MetaAhorro m) {
        var e = repository.findById(m.id()).orElseGet(MetaAhorroJpaEntity::new);
        e.setId(m.id());
        e.setHogarId(m.hogarId());
        e.setNombre(m.nombre());
        e.setProposito(m.proposito());
        e.setTipo(m.tipo().name());
        e.setMontoObjetivo(m.montoObjetivo().valor());
        e.setFechaObjetivo(m.fechaObjetivo());
        e.setEsPrincipal(m.esPrincipal());
        e.setEstado(m.estado().name());
        return aDominio(repository.saveAndFlush(e));
    }

    @Override
    public Optional<MetaAhorro> porId(UUID hogarId, UUID metaId) {
        return repository.findByIdAndHogarId(metaId, hogarId).map(MetaPersistenceAdapter::aDominio);
    }

    @Override
    public Optional<MetaAhorro> porIdSinHogar(UUID metaId) {
        return repository.findById(metaId).map(MetaPersistenceAdapter::aDominio);
    }

    @Override
    public List<MetaAhorro> delHogar(UUID hogarId) {
        return repository.findByHogarIdAndEstadoNot(hogarId, EstadoMeta.CANCELADA.name()).stream()
                .map(MetaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<MetaAhorro> principalActiva(UUID hogarId) {
        return repository.findFirstByHogarIdAndEsPrincipalTrueAndEstado(hogarId, EstadoMeta.ACTIVA.name())
                .map(MetaPersistenceAdapter::aDominio);
    }

    @Override
    public void quitarPrincipal(UUID hogarId) {
        repository.quitarPrincipal(hogarId);
    }

    private static MetaAhorro aDominio(MetaAhorroJpaEntity e) {
        return new MetaAhorro(e.getId(), e.getHogarId(), e.getNombre(), e.getProposito(), TipoMeta.valueOf(e.getTipo()),
                Dinero.de(e.getMontoObjetivo()), e.getFechaObjetivo(), e.isEsPrincipal(), EstadoMeta.valueOf(e.getEstado()));
    }
}
