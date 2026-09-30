package com.selahfinance.metas.application.port.in;

import java.util.UUID;

/** Se invoca cuando cambian los aportes de una meta (aporte registrado o eliminado). */
public interface ReevaluarMetaUseCase {

    /** ACTIVA → COMPLETADA al alcanzar el objetivo; COMPLETADA → ACTIVA si un aporte se elimina. */
    void reevaluar(UUID metaId);
}
