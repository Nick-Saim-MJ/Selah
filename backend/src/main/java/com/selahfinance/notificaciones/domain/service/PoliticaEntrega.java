package com.selahfinance.notificaciones.domain.service;

import com.selahfinance.notificaciones.domain.model.CategoriaNotificacion;
import com.selahfinance.shared.domain.CalendarioSabado;
import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * Modo Sábado (spec 4.8): entre el viernes al atardecer y el sábado al atardecer no se envían
 * avisos de consumo. No se descartan: se posponen hasta que termina el sábado. No bloquea el
 * registro manual de datos; solo evita promover el gasto.
 */
public final class PoliticaEntrega {

    private PoliticaEntrega() {
    }

    public static Instant programar(CategoriaNotificacion categoria, ZonedDateTime ahora, boolean modoSabadoActivo) {
        if (categoria == CategoriaNotificacion.CONSUMO && modoSabadoActivo && CalendarioSabado.enModoSabado(ahora)) {
            return CalendarioSabado.finDeModoSabado(ahora).toInstant();
        }
        return ahora.toInstant();
    }
}
