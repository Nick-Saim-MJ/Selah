package com.selahfinance.iglesias.application.usecase;

import com.selahfinance.iglesias.application.port.in.ConsultarIglesiasUseCase;
import com.selahfinance.iglesias.application.port.in.GestionarIglesiasUseCase;
import com.selahfinance.iglesias.application.port.out.IglesiaRepositoryPort;
import com.selahfinance.iglesias.domain.model.Iglesia;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import com.selahfinance.shared.domain.DomainException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class IglesiasService implements ConsultarIglesiasUseCase, GestionarIglesiasUseCase {

    private final IglesiaRepositoryPort iglesias;

    @Override
    @Transactional(readOnly = true)
    public List<Iglesia> activas() {
        return iglesias.activas();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Iglesia> todas() {
        return iglesias.todas();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Iglesia> porId(UUID id) {
        return iglesias.porId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeActiva(UUID id) {
        return id != null && iglesias.porId(id).map(Iglesia::activa).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> nombres(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return iglesias.porIds(ids).stream().collect(Collectors.toMap(Iglesia::id, Iglesia::nombre, (a, b) -> a));
    }

    @Override
    @Transactional
    public Iglesia crear(String nombre, String ciudad, String distrito) {
        var nueva = Iglesia.nueva(nombre, ciudad, distrito);
        verificarNombreLibre(nueva, null);
        return iglesias.guardar(nueva);
    }

    @Override
    @Transactional
    public Iglesia actualizar(UUID id, String nombre, String ciudad, String distrito, boolean activa) {
        var actual = iglesias.porId(id).orElseThrow(() -> new RecursoNoEncontradoException("Iglesia", id));
        var cambiada = actual.actualizar(nombre, ciudad, distrito, activa);
        verificarNombreLibre(cambiada, id);
        return iglesias.guardar(cambiada);
    }

    private void verificarNombreLibre(Iglesia iglesia, UUID excluirId) {
        if (iglesias.existeNombre(iglesia.nombre(), iglesia.ciudad(), excluirId)) {
            throw new DomainException("IGLESIA_DUPLICADA", "Ya existe una iglesia con ese nombre en esa ciudad");
        }
    }
}
