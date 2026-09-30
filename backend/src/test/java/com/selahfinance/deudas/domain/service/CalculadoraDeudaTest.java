package com.selahfinance.deudas.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.selahfinance.shared.domain.Dinero;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CalculadoraDeudaTest {

    private final CalculadoraDeuda calculadora = new CalculadoraDeuda();

    @Test
    void sinInteresLaCuotaEsSaldoEntrePlazo() {
        assertThat(calculadora.cuotaMensual(Dinero.de("2400"), BigDecimal.ZERO, 8)).isEqualTo(Dinero.de("300"));
    }

    @Test
    void cuotaYMesesRestantesSonConsistentes() {
        var saldo = Dinero.de("2400");
        var tea = BigDecimal.valueOf(24);

        var cuota = calculadora.cuotaMensual(saldo, tea, 8);

        assertThat(cuota.valor()).isBetween(new BigDecimal("320"), new BigDecimal("330"));
        assertThat(calculadora.mesesRestantes(saldo, tea, cuota)).hasValue(8);
    }

    @Test
    void cuotaQueNoCubreInteresesNuncaTermina() {
        assertThat(calculadora.mesesRestantes(Dinero.de("10000"), BigDecimal.valueOf(60), Dinero.de("10"))).isEmpty();
    }

    @Test
    void alertaDeSobreendeudamientoSobreCuarentaPorCiento() {
        assertThat(calculadora.superaLimiteEndeudamiento(Dinero.de("1900"), Dinero.de("4500"))).isTrue();
        assertThat(calculadora.superaLimiteEndeudamiento(Dinero.de("300"), Dinero.de("4500"))).isFalse();
    }
}
