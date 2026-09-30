package com.selahfinance.iglesias.application.port.in;

import com.selahfinance.iglesias.domain.model.Iglesia;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ConsultarIglesiasUseCase {

    /** Solo las activas (lista pública para el registro). */
    List<Iglesia> activas();

    /** Todas, para el panel del admin. */
    List<Iglesia> todas();

    Optional<Iglesia> porId(UUID id);

    boolean existeActiva(UUID id);

    /** id → nombre, para mostrar el nombre de la iglesia en listados de otros módulos. */
    Map<UUID, String> nombres(Collection<UUID> ids);
}
