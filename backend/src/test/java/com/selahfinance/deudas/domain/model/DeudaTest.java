package com.selahfinance.deudas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeudaTest {

    private static Deuda sinInteres() {
        return Deuda.nueva(UUID.randomUUID(), "Préstamo amigo", null, Dinero.de("1000"), null, BigDecimal.ZERO, 10,
                LocalDate.of(2026, 1, 1), 15);
    }

    private static Deuda conInteres() {
        return Deuda.nueva(UUID.randomUUID(), "Tarjeta", "Banco", Dinero.de("3600"), Dinero.de("2400"),
                BigDecimal.valueOf(24), 8, LocalDate.of(2026, 5, 10), 10);
    }

    @Test
    void laCuotaSeCalculaYNoSeDigita() {
        assertThat(sinInteres().cuotaMensual()).isEqualTo(Dinero.de("100"));
        assertThat(conInteres().cuotaMensual().valor()).isBetween(new BigDecimal("320"), new BigDecimal("330"));
    }

    @Test
    void sinInteresTodoElPagoReduceElCapital() {
        var r = sinInteres().aplicarPago(Dinero.de("100"));

        assertThat(r.detalle().interes()).isEqualTo(Dinero.CERO);
        assertThat(r.detalle().capital()).isEqualTo(Dinero.de("100"));
        assertThat(r.deuda().saldoActual()).isEqualTo(Dinero.de("900"));
    }

    @Test
    void conInteresElPagoCubrePrimeroLosInteresesDelMes() {
        var deuda = conInteres();
        var interes = deuda.interesDelMes();

        var r = deuda.aplicarPago(Dinero.de("300"));

        assertThat(r.detalle().interes()).isEqualTo(interes);
        assertThat(r.detalle().capital()).isEqualTo(Dinero.de("300").restar(interes));
        assertThat(r.detalle().total()).isEqualTo(Dinero.de("300"));
        assertThat(r.deuda().saldoActual()).isEqualTo(Dinero.de("2400").restar(r.detalle().capital()));
    }

    @Test
    void unPagoMenorAlInteresNoReduceElCapital() {
        var r = conInteres().aplicarPago(Dinero.de("10"));

        assertThat(r.detalle().capital()).isEqualTo(Dinero.CERO);
        assertThat(r.deuda().saldoActual()).isEqualTo(Dinero.de("2400"));
    }

    @Test
    void noSePuedePagarMasDeLoQueSeDebe() {
        assertThatThrownBy(() -> sinInteres().aplicarPago(Dinero.de("1000.01"))).isInstanceOf(DomainException.class);
    }

    @Test
    void alPagarTodoLaDeudaQuedaPagadaYYaNoAceptaPagos() {
        var pagada = sinInteres().aplicarPago(Dinero.de("1000")).deuda();

        assertThat(pagada.estado()).isEqualTo(EstadoDeuda.PAGADA);
        assertThatThrownBy(() -> pagada.aplicarPago(Dinero.de("1"))).isInstanceOf(DomainException.class);
    }

    @Test
    void revertirUnPagoDevuelveElCapitalYReactivaLaDeuda() {
        var original = sinInteres();
        var r = original.aplicarPago(Dinero.de("1000"));

        var revertida = r.deuda().revertirPago(r.detalle());

        assertThat(revertida.saldoActual()).isEqualTo(original.saldoActual());
        assertThat(revertida.estado()).isEqualTo(EstadoDeuda.ACTIVA);
    }

    @Test
    void elSaldoNoPuedeSuperarElMontoOriginal() {
        assertThatThrownBy(() -> Deuda.nueva(UUID.randomUUID(), "X", null, Dinero.de("100"), Dinero.de("200"),
                BigDecimal.ZERO, 5, LocalDate.now(), null)).isInstanceOf(DomainException.class);
    }

    @Test
    void mesesRestantesYPorcentajePagado() {
        var deuda = sinInteres();

        assertThat(deuda.mesesRestantes()).hasValue(10);
        assertThat(deuda.aplicarPago(Dinero.de("250")).deuda().porcentajePagado()).isEqualByComparingTo("25");
    }
}
