package com.selahfinance.notificaciones.application.port.in;

import com.selahfinance.notificaciones.domain.model.Notificacion;
import com.selahfinance.shared.application.Pagina;
import java.util.UUID;

public interface ConsultarBandejaUseCase {

    /** Avisos ya visibles (los pospuestos por el Modo Sábado aún no aparecen), del más nuevo al más viejo. */
    Pagina<Notificacion> bandeja(UUID usuarioId, int pagina, int tamanio);

    long noLeidas(UUID usuarioId);

    void marcarLeida(UUID usuarioId, UUID notificacionId);

    void marcarTodasLeidas(UUID usuarioId);
}
