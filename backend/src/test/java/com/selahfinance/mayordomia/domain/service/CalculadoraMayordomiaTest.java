package com.selahfinance.mayordomia.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.selahfinance.mayordomia.domain.model.EstadoApartado;
import com.selahfinance.mayordomia.domain.model.PoliticaMayordomia;
import com.selahfinance.mayordomia.domain.model.TipoApartado;
import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CalculadoraMayordomiaTest {

    private final CalculadoraMayordomia calculadora = new CalculadoraMayordomia();
    private final PoliticaMayordomia diezPorCientoMasDos = new PoliticaMayordomia(BigDecimal.TEN, BigDecimal.valueOf(2));

    @Test
    void desglosaElIngresoComoEnElPrototipo() {
        var d = calculadora.desglosar(Dinero.de("4500"), diezPorCientoMasDos);

        assertThat(d.diezmo()).isEqualTo(Dinero.de("450"));
        assertThat(d.ofrenda()).isEqualTo(Dinero.de("90"));
        assertThat(d.disponible()).isEqualTo(Dinero.de("3960"));
    }

    @Test
    void redondeaAlCentimo() {
        var d = calculadora.desglosar(Dinero.de("1234.57"), diezPorCientoMasDos);

        assertThat(d.diezmo()).isEqualTo(Dinero.de("123.46"));
        assertThat(d.ofrenda()).isEqualTo(Dinero.de("24.69"));
        assertThat(d.disponible()).isEqualTo(Dinero.de("1086.42"));
    }

    @Test
    void generaApartadosPendientesYOmiteLaOfrendaSiEsCero() {
        var sinOfrenda = new PoliticaMayordomia(BigDecimal.TEN, BigDecimal.ZERO);

        var apartados = calculadora.generarApartados(UUID.randomUUID(), UUID.randomUUID(), Dinero.de("1000"),
                LocalDate.of(2026, 9, 17), sinOfrenda);

        assertThat(apartados).singleElement().satisfies(a -> {
            assertThat(a.tipo()).isEqualTo(TipoApartado.DIEZMO);
            assertThat(a.monto()).isEqualTo(Dinero.de("100"));
            assertThat(a.estado()).isEqualTo(EstadoApartado.PENDIENTE);
            assertThat(a.periodo().inicio()).isEqualTo(LocalDate.of(2026, 9, 1));
        });
    }

    @Test
    void rechazaIngresosNoPositivos() {
        assertThatThrownBy(() -> calculadora.desglosar(Dinero.CERO, diezPorCientoMasDos))
                .isInstanceOf(DomainException.class);
    }
}
