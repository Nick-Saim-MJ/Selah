package com.selahfinance.metas.application.port.out;

import com.selahfinance.shared.domain.Dinero;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/** Suma de los movimientos APORTE_META (fuente única). */
public interface AportesMetaPort {

    Map<UUID, Dinero> montosAportados(Collection<UUID> metaIds);

    default Dinero montoAportado(UUID metaId) {
        return montosAportados(java.util.List.of(metaId)).getOrDefault(metaId, Dinero.CERO);
    }
}
