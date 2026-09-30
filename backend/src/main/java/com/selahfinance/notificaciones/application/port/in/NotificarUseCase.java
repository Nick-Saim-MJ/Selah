package com.selahfinance.notificaciones.application.port.in;

import com.selahfinance.notificaciones.domain.model.CategoriaNotificacion;
import com.selahfinance.notificaciones.domain.model.Notificacion;
import com.selahfinance.notificaciones.domain.model.TipoNotificacion;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface NotificarUseCase {

    /**
     * Crea el aviso respetando las preferencias del usuario, el Modo Sábado y la clave de duplicado
     * (un mismo aviso no se repite). Devuelve vacío si no se creó.
     */
    Optional<Notificacion> notificar(Comando comando);

    record Comando(
            UUID usuarioId,
            UUID hogarId,
            TipoNotificacion tipo,
            CategoriaNotificacion categoria,
            String titulo,
            String cuerpo,
            Map<String, String> datos,
            String claveDedupe) {
    }
}
