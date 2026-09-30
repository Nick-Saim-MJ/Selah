package com.selahfinance.shared.domain;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/** Mes contable. Se persiste como el primer día del mes. */
public record Periodo(YearMonth mes) {

    public Periodo {
        Objects.requireNonNull(mes, "mes");
    }

    public static Periodo de(LocalDate fecha) {
        return new Periodo(YearMonth.from(fecha));
    }

    public static Periodo parse(String texto) {
        return new Periodo(YearMonth.parse(texto));
    }

    public LocalDate inicio() {
        return mes.atDay(1);
    }

    public LocalDate fin() {
        return mes.atEndOfMonth();
    }

    @Override
    public String toString() {
        return mes.toString();
    }
}
