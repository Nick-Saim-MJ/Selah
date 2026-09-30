package com.selahfinance.reportes.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.Periodo;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReporteFinancieroTest {

    private static final Periodo SEPTIEMBRE = Periodo.parse("2026-09");

    private static ResumenMes mes(String ingresos, String gastos, String deuda, String ahorro) {
        return new ResumenMes(new IndicadoresTesoro(Dinero.de(ingresos), Dinero.de(gastos), Dinero.de(deuda),
                Dinero.de(ahorro), Dinero.CERO, Dinero.CERO, Dinero.CERO), Dinero.de("1000"), Dinero.CERO, Dinero.CERO);
    }

    private static LineaPresupuesto linea(String nombre, String planeado, String real) {
        return new LineaPresupuesto(UUID.randomUUID(), nombre, "#112233", Dinero.de(planeado), Dinero.de(real));
    }

    @Test
    void lasCifrasDelPrototipoDanSemaforoVerde() {
        // Ingreso 4 500; gastos 2 530 + deuda 300 => 62.9% (< 70%)
        var r = ReporteFinanciero.armar(SEPTIEMBRE, mes("4500", "2530", "300", "300"), List.of());

        assertThat(r.semaforo()).contains(Semaforo.VERDE);
        assertThat(r.ratioGasto()).isEqualByComparingTo("56.2");
        assertThat(r.ratioDeuda()).isEqualByComparingTo("6.7");
        assertThat(r.ratioAhorro()).isEqualByComparingTo("6.7");
    }

    @Test
    void sinIngresosNoHayVeredicto() {
        var r = ReporteFinanciero.armar(SEPTIEMBRE, mes("0", "100", "0", "0"), List.of());

        assertThat(r.semaforo()).isEmpty();
        assertThat(r.ratioGasto()).isEqualByComparingTo("0");
    }

    @Test
    void gastarMasDelNoventaPorCientoEsRojo() {
        assertThat(ReporteFinanciero.armar(SEPTIEMBRE, mes("1000", "850", "100", "0"), List.of()).semaforo())
                .contains(Semaforo.ROJO);
        assertThat(ReporteFinanciero.armar(SEPTIEMBRE, mes("1000", "700", "0", "0"), List.of()).semaforo())
                .contains(Semaforo.AMARILLO);
    }

    @Test
    void elPrincipalDesvioEsLaCategoriaQueMasSePasoDeSuPresupuesto() {
        var lineas = List.of(linea("Vivienda", "900", "900"), linea("Alimentación", "700", "780"),
                linea("Transporte", "300", "310"), linea("Ocio", "0", "500"));

        var r = ReporteFinanciero.armar(SEPTIEMBRE, mes("4500", "2000", "0", "0"), lineas);

        assertThat(r.principalDesvio()).hasValueSatisfying(l -> assertThat(l.nombre()).isEqualTo("Alimentación"));
        assertThat(lineas.get(1).variacionPct()).isEqualByComparingTo("11.4");
        assertThat(lineas.get(3).variacionPct()).isNull(); // sin presupuesto no hay % de variación
        assertThat(lineas.get(0).excedido()).isFalse();
    }
}
