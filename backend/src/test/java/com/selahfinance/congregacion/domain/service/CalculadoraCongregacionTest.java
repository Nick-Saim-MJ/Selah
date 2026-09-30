package com.selahfinance.congregacion.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.selahfinance.reportes.domain.model.PuntajesMayordomia;
import com.selahfinance.reportes.domain.model.ReporteMayordomia;
import com.selahfinance.shared.domain.Periodo;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CalculadoraCongregacionTest {

    private static final Periodo SEPTIEMBRE = Periodo.parse("2026-09");

    private static ReporteMayordomia reporte(Integer tiempo, Integer talento, int tesoro, Integer templo, Boolean diezmoAlDia) {
        return new ReporteMayordomia(UUID.randomUUID(), UUID.randomUUID(), SEPTIEMBRE,
                new PuntajesMayordomia(dec(tiempo), dec(talento), BigDecimal.valueOf(tesoro), dec(templo)), Map.of(),
                diezmoAlDia, Instant.parse("2026-09-28T12:00:00Z"), "1.0");
    }

    private static BigDecimal dec(Integer valor) {
        return valor == null ? null : BigDecimal.valueOf(valor);
    }

    @Test
    void conPocosMiembrosNoSeMuestranPromediosParaNoIdentificarAPersonas() {
        var resumen = CalculadoraCongregacion.resumir(SEPTIEMBRE, 40, List.of(reporte(80, 70, 90, 60, true),
                reporte(50, 50, 50, 50, false)), 3);

        assertThat(resumen.datosSuficientes()).isFalse();
        assertThat(resumen.miembrosActivos()).isEqualTo(40);
        assertThat(resumen.miembrosConReporte()).isEqualTo(2);
        assertThat(resumen.global()).isNull();
        assertThat(resumen.tiempo()).isNull();
        assertThat(resumen.fidelidadDiezmoPct()).isNull();
    }

    @Test
    void conSuficientesMiembrosPromediaCadaDimension() {
        var resumen = CalculadoraCongregacion.resumir(SEPTIEMBRE, 10, List.of(
                reporte(80, 60, 90, 70, true), reporte(60, 40, 70, 50, true), reporte(70, 50, 80, 60, false)), 3);

        assertThat(resumen.datosSuficientes()).isTrue();
        assertThat(resumen.tiempo()).isEqualByComparingTo("70");
        assertThat(resumen.talento()).isEqualByComparingTo("50");
        assertThat(resumen.tesoro()).isEqualByComparingTo("80");
        assertThat(resumen.templo()).isEqualByComparingTo("60");
        assertThat(resumen.fidelidadDiezmoPct()).isEqualByComparingTo("66.7");
    }

    @Test
    void unaDimensionConPocosDatosNoSePromedia() {
        // Solo 2 de 3 tienen dato de Talento: promediarlo revelaría a esas dos personas
        var resumen = CalculadoraCongregacion.resumir(SEPTIEMBRE, 5, List.of(
                reporte(80, 60, 90, 70, true), reporte(60, 40, 70, 50, true), reporte(70, null, 80, 60, true)), 3);

        assertThat(resumen.datosSuficientes()).isTrue();
        assertThat(resumen.talento()).isNull();
        assertThat(resumen.tiempo()).isNotNull();
    }

    @Test
    void laFidelidadIgnoraAQuienesNoTuvieronDiezmoEseMes() {
        var resumen = CalculadoraCongregacion.resumir(SEPTIEMBRE, 5, List.of(
                reporte(80, 60, 90, 70, null), reporte(60, 40, 70, 50, null), reporte(70, 50, 80, 60, true)), 3);

        // Solo una persona tuvo diezmo: no se publica un porcentaje que sería de una sola persona
        assertThat(resumen.fidelidadDiezmoPct()).isNull();
    }
}
