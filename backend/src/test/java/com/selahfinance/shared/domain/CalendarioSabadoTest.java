package com.selahfinance.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class CalendarioSabadoTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    /** 2026-10-02 es viernes. */
    private static ZonedDateTime en(int dia, int hora, int minuto) {
        return ZonedDateTime.of(2026, 10, dia, hora, minuto, 0, 0, LIMA);
    }

    @Test
    void modoSabadoVaDeViernesAlAtardecerASabadoAlAtardecer() {
        assertThat(CalendarioSabado.enModoSabado(en(2, 17, 59))).isFalse();
        assertThat(CalendarioSabado.enModoSabado(en(2, 18, 0))).isTrue();
        assertThat(CalendarioSabado.enModoSabado(en(3, 9, 0))).isTrue();
        assertThat(CalendarioSabado.enModoSabado(en(3, 17, 59))).isTrue();
        assertThat(CalendarioSabado.enModoSabado(en(3, 18, 0))).isFalse();
        assertThat(CalendarioSabado.enModoSabado(en(5, 12, 0))).isFalse(); // lunes
    }

    @Test
    void elFinDeLaVentanaEsElAtardecerDelSabado() {
        assertThat(CalendarioSabado.finDeModoSabado(en(2, 19, 0))).isEqualTo(en(3, 18, 0));
        assertThat(CalendarioSabado.finDeModoSabado(en(3, 10, 0))).isEqualTo(en(3, 18, 0));
    }

    @Test
    void laReflexionApareceElViernesPorLaTardeYTodoElSabado() {
        assertThat(CalendarioSabado.reflexionVisible(en(2, 14, 59))).isFalse();
        assertThat(CalendarioSabado.reflexionVisible(en(2, 15, 0))).isTrue();
        assertThat(CalendarioSabado.reflexionVisible(en(3, 23, 0))).isTrue();
        assertThat(CalendarioSabado.reflexionVisible(en(4, 8, 0))).isFalse(); // domingo
    }

    @Test
    void laSemanaEmpiezaElDomingo() {
        assertThat(CalendarioSabado.inicioDeSemana(LocalDate.of(2026, 10, 3))).isEqualTo(LocalDate.of(2026, 9, 27));
        assertThat(CalendarioSabado.inicioDeSemana(LocalDate.of(2026, 9, 27))).isEqualTo(LocalDate.of(2026, 9, 27));
    }
}
