package com.selahfinance.iglesias.infrastructure.persistence;

import com.selahfinance.iglesias.application.port.out.IglesiaRepositoryPort;
import com.selahfinance.iglesias.domain.model.Iglesia;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class IglesiaPersistenceAdapter implements IglesiaRepositoryPort {

    private final IglesiaJpaRepository repository;

    @Override
    public Iglesia guardar(Iglesia iglesia) {
        var entidad = repository.findById(iglesia.id()).orElseGet(IglesiaJpaEntity::new);
        entidad.setId(iglesia.id());
        entidad.setNombre(iglesia.nombre());
        entidad.setCiudad(iglesia.ciudad());
        entidad.setDistrito(iglesia.distrito());
        entidad.setActiva(iglesia.activa());
        return aDominio(repository.save(entidad));
    }

    @Override
    public Optional<Iglesia> porId(UUID id) {
        return repository.findById(id).map(IglesiaPersistenceAdapter::aDominio);
    }

    @Override
    public List<Iglesia> activas() {
        return repository.findByActivaTrueOrderByNombreAsc().stream().map(IglesiaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Iglesia> todas() {
        return repository.findAllByOrderByNombreAsc().stream().map(IglesiaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Iglesia> porIds(Collection<UUID> ids) {
        return repository.findAllById(ids).stream().map(IglesiaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public boolean existeNombre(String nombre, String ciudad, UUID excluirId) {
        return repository.existeNombre(nombre, ciudad, excluirId);
    }

    private static Iglesia aDominio(IglesiaJpaEntity e) {
        return new Iglesia(e.getId(), e.getNombre(), e.getCiudad(), e.getDistrito(), e.isActiva());
    }
}
