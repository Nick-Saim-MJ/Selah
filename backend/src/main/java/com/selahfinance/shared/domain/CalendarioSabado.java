package com.selahfinance.shared.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * Reglas del sábado (Éx. 20:8-11, Lev. 23:3). El atardecer se aproxima con una hora fija (18:00
 * en la zona del hogar): en Perú varía menos de una hora en todo el año. Si más adelante se usa
 * latitud/longitud del hogar, solo cambia {@link #atardecer(LocalDate)}.
 */
public final class CalendarioSabado {

    private static final LocalTime ATARDECER = LocalTime.of(18, 0);
    private static final LocalTime INICIO_TARDE_VIERNES = LocalTime.of(15, 0);

    private CalendarioSabado() {
    }

    /** De viernes al atardecer a sábado al atardecer: no se promueve el consumo. */
    public static boolean enModoSabado(ZonedDateTime ahora) {
        var hora = ahora.toLocalTime();
        return switch (ahora.getDayOfWeek()) {
            case FRIDAY -> !hora.isBefore(atardecer(ahora.toLocalDate()));
            case SATURDAY -> hora.isBefore(atardecer(ahora.toLocalDate()));
            default -> false;
        };
    }

    /** Momento en que termina la ventana de Modo Sábado en curso (o de la próxima si no hay una activa). */
    public static ZonedDateTime finDeModoSabado(ZonedDateTime ahora) {
        var sabado = ahora.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        return sabado.atTime(atardecer(sabado)).atZone(ahora.getZone());
    }

    /** La tarjeta de reflexión aparece el viernes por la tarde y todo el sábado (spec 4.9). */
    public static boolean reflexionVisible(ZonedDateTime ahora) {
        return switch (ahora.getDayOfWeek()) {
            case FRIDAY -> !ahora.toLocalTime().isBefore(INICIO_TARDE_VIERNES);
            case SATURDAY -> true;
            default -> false;
        };
    }

    /** Domingo que abre la semana de la fecha dada (clave de la reflexión semanal). */
    public static LocalDate inicioDeSemana(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }

    static LocalTime atardecer(LocalDate fecha) {
        return ATARDECER;
    }
}
