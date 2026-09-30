package com.selahfinance.inicio.application.port.in;

import com.selahfinance.inicio.application.dto.InicioResult;
import java.util.UUID;

public interface ConsultarInicioUseCase {

    /** Resumen del mes en curso: semáforo, saldo disponible, diezmo pendiente, meta principal y reflexión. */
    InicioResult inicio(UUID usuarioId, UUID hogarId);
}
