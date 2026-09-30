package com.selahfinance.habitos.domain.model;

import java.math.BigDecimal;

/** Un hábito con lo registrado hoy y en la semana, y su cumplimiento frente a la meta de su frecuencia. */
public record HabitoDelDia(Habito habito, BigDecimal valorHoy, BigDecimal valorSemana) {

    public BigDecimal cumplimientoPct() {
        BigDecimal valor = habito.frecuenciaMeta() == FrecuenciaMeta.DIARIA ? valorHoy : valorSemana;
        return Habito.cumplimientoPct(valor, habito.metaValor());
    }
}
