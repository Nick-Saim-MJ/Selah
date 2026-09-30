package com.selahfinance.habitos.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.selahfinance.shared.domain.DimensionMayordomia;
import com.selahfinance.shared.domain.DomainException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HabitoTest {

    private static Habito habito(UnidadHabito unidad, FrecuenciaMeta frecuencia, String meta) {
        return new Habito(UUID.randomUUID(), "X", DimensionMayordomia.TEMPLO, "Hábito", null, unidad, frecuencia,
                new BigDecimal(meta), null, 1);
    }

    @Test
    void laMetaDiariaSeMultiplicaPorLosDiasDelRango() {
        var agua = habito(UnidadHabito.VASOS, FrecuenciaMeta.DIARIA, "8");

        assertThat(agua.metaParaRango(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10))).isEqualByComparingTo("80");
    }

    @Test
    void laMetaSemanalSeProrrateaPorDias() {
        var iglesia = habito(UnidadHabito.VECES, FrecuenciaMeta.SEMANAL, "2");

        assertThat(iglesia.metaParaRango(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 14))).isEqualByComparingTo("4");
        assertThat(iglesia.metaParaRango(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 8, 1))).isEqualByComparingTo("0");
    }

    @Test
    void unHabitoBooleanoSoloAdmiteCeroOUno() {
        var culto = habito(UnidadHabito.BOOLEANO, FrecuenciaMeta.DIARIA, "1");

        culto.validarValor(BigDecimal.ONE);
        culto.validarValor(BigDecimal.ZERO);
        assertThatThrownBy(() -> culto.validarValor(new BigDecimal("2"))).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> culto.validarValor(new BigDecimal("-1"))).isInstanceOf(DomainException.class);
    }

    @Test
    void elCumplimientoTieneTopeDeCienPorCiento() {
        assertThat(Habito.cumplimientoPct(new BigDecimal("450"), new BigDecimal("900"))).isEqualByComparingTo("50");
        assertThat(Habito.cumplimientoPct(new BigDecimal("2000"), new BigDecimal("900"))).isEqualByComparingTo("100");
        assertThat(Habito.cumplimientoPct(BigDecimal.TEN, BigDecimal.ZERO)).isEqualByComparingTo("0");
    }

    @Test
    void elCumplimientoDelDiaUsaLaFrecuenciaDeLaMeta() {
        var diario = new HabitoDelDia(habito(UnidadHabito.MINUTOS, FrecuenciaMeta.DIARIA, "30"), new BigDecimal("15"),
                new BigDecimal("100"));
        var semanal = new HabitoDelDia(habito(UnidadHabito.HORAS, FrecuenciaMeta.SEMANAL, "4"), new BigDecimal("1"),
                new BigDecimal("2"));

        assertThat(diario.cumplimientoPct()).isEqualByComparingTo("50");
        assertThat(semanal.cumplimientoPct()).isEqualByComparingTo("50");
    }
}
