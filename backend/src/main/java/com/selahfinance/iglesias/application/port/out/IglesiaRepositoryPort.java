package com.selahfinance.iglesias.application.port.out;

import com.selahfinance.iglesias.domain.model.Iglesia;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IglesiaRepositoryPort {

    Iglesia guardar(Iglesia iglesia);

    Optional<Iglesia> porId(UUID id);

    List<Iglesia> activas();

    List<Iglesia> todas();

    List<Iglesia> porIds(Collection<UUID> ids);

    /** ¿Ya hay otra iglesia con ese nombre en esa ciudad? (sin distinguir mayúsculas) */
    boolean existeNombre(String nombre, String ciudad, UUID excluirId);
}
