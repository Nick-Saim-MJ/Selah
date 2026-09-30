package com.selahfinance.metas.application.port.out;

import com.selahfinance.metas.domain.model.MetaAhorro;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetaRepositoryPort {

    MetaAhorro guardar(MetaAhorro meta);

    Optional<MetaAhorro> porId(UUID hogarId, UUID metaId);

    Optional<MetaAhorro> porIdSinHogar(UUID metaId);

    /** No canceladas. */
    List<MetaAhorro> delHogar(UUID hogarId);

    Optional<MetaAhorro> principalActiva(UUID hogarId);

    /** Quita la marca de principal a las demás metas del hogar (una sola principal activa). */
    void quitarPrincipal(UUID hogarId);
}
