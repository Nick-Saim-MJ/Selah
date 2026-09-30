package com.selahfinance.identidad.application.port.in;

import com.selahfinance.identidad.application.dto.UsuarioDetalle;
import java.util.UUID;

public interface GestionarPerfilUseCase {

    UsuarioDetalle obtener(UUID usuarioId);

    /** Solo HERMANO. Queda registrada la fecha desde la que comparte. */
    UsuarioDetalle compartirReporte(UUID usuarioId, boolean compartir);

    /** Marca terminado el asistente de configuración inicial. */
    UsuarioDetalle completarConfiguracionInicial(UUID usuarioId);
}
