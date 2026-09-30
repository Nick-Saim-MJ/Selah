package com.selahfinance.reportes.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.selahfinance.reportes.domain.model.CumplimientoHabito;
import com.selahfinance.reportes.domain.model.IndicadoresTesoro;
import com.selahfinance.reportes.domain.model.PuntajesMayordomia;
import com.selahfinance.reportes.domain.model.Semaforo;
import com.selahfinance.shared.domain.Dinero;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalculadoraPuntajesTest {

    private final CalculadoraPuntajes calculadora = new CalculadoraPuntajes();

    @Test
    void semaforoFinancieroUsaLosUmbralesSbs() {
        assertThat(Semaforo.porRatioGasto(BigDecimal.valueOf(63))).isEqualTo(Semaforo.VERDE);
        assertThat(Semaforo.porRatioGasto(BigDecimal.valueOf(82))).isEqualTo(Semaforo.AMARILLO);
        assertThat(Semaforo.porRatioGasto(BigDecimal.valueOf(97))).isEqualTo(Semaforo.ROJO);
    }

    @Test
    void tesoroPerfectoCuandoTodoEstaEnOrden() {
        var i = new IndicadoresTesoro(Dinero.de("4500"), Dinero.de("2000"), Dinero.de("300"), Dinero.de("500"),
                Dinero.de("450"), Dinero.de("450"), Dinero.de("300"));

        assertThat(calculadora.puntajeTesoro(i)).isEqualByComparingTo("100");
    }

    @Test
    void sinIngresosNoHayPuntajeDeTesoro() {
        var i = new IndicadoresTesoro(Dinero.CERO, Dinero.CERO, Dinero.CERO, Dinero.CERO, Dinero.CERO, Dinero.CERO,
                Dinero.CERO);

        assertThat(calculadora.puntajeTesoro(i)).isNull();
    }

    @Test
    void habitosPromedianCumplimientoConTopeDeCien() {
        var puntaje = calculadora.puntajeHabitos(List.of(
                new CumplimientoHabito("AGUA", "Agua", BigDecimal.valueOf(240), BigDecimal.valueOf(300), "APP"),
                new CumplimientoHabito("EJERCICIO", "Ejercicio", BigDecimal.valueOf(900), BigDecimal.valueOf(450), "modulo-salud")));

        assertThat(puntaje).isEqualByComparingTo("75");
    }

    @Test
    void elGlobalIgnoraDimensionesSinDatos() {
        var p = new PuntajesMayordomia(BigDecimal.valueOf(80), null, BigDecimal.valueOf(90), BigDecimal.valueOf(70));

        assertThat(p.global()).hasValueSatisfying(g -> assertThat(g).isEqualByComparingTo("80"));
        assertThat(p.semaforo()).contains(Semaforo.VERDE);
    }
}
