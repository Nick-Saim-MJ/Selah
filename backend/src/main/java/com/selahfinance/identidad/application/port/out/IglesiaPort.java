package com.selahfinance.identidad.application.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/** Lo que identidad necesita del módulo iglesias (lo implementa un adaptador). */
public interface IglesiaPort {

    boolean existeActiva(UUID iglesiaId);

    Map<UUID, String> nombres(Collection<UUID> iglesiaIds);
}
