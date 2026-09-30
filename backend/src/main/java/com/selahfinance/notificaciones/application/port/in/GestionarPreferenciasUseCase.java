package com.selahfinance.notificaciones.application.port.in;

import com.selahfinance.notificaciones.domain.model.PreferenciasNotificacion;
import java.util.UUID;

public interface GestionarPreferenciasUseCase {

    PreferenciasNotificacion obtener(UUID usuarioId);

    PreferenciasNotificacion actualizar(PreferenciasNotificacion preferencias);
}
