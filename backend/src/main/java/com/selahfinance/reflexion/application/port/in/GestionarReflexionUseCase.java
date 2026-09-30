package com.selahfinance.reflexion.application.port.in;

import com.selahfinance.reflexion.domain.model.TarjetaReflexion;
import java.util.UUID;

public interface GestionarReflexionUseCase {

    /** La tarjeta de la semana en curso (con la respuesta si ya escribió una). */
    TarjetaReflexion actual(UUID usuarioId, UUID hogarId);

    /** Guarda o reemplaza la respuesta de la semana. Es opcional: nunca se exige. */
    TarjetaReflexion responder(UUID usuarioId, UUID hogarId, String respuesta);
}
