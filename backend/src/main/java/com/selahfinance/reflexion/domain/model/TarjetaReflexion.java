package com.selahfinance.reflexion.domain.model;

import java.time.LocalDate;

/**
 * La tarjeta de reflexión de la semana. {@code visible} indica si toca mostrarla en Inicio
 * (viernes por la tarde y sábado); la pantalla de reflexión se puede abrir siempre desde Configuración.
 * Es solo lectura + una pregunta opcional: no hay entrada de datos financieros aquí.
 */
public record TarjetaReflexion(
        boolean visible,
        LocalDate semanaInicio,
        PreguntaReflexion pregunta,
        String respuesta,
        ResumenSemana resumen) {
}
