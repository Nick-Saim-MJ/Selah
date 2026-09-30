package com.selahfinance.movimientos.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MovimientoTest {

    private static DatosMovimiento datos(TipoMovimiento tipo, String monto, UUID categoriaId) {
        return new DatosMovimiento(UUID.randomUUID(), UUID.randomUUID(), tipo, Dinero.de(monto),
                LocalDate.of(2026, 9, 15), "Supermercado", null, categoriaId, null, null, null, null, true,
                OrigenMovimiento.APP, null);
    }

    @Test
    void unGastoExigeCategoria() {
        assertThatThrownBy(() -> Movimiento.registrar(datos(TipoMovimiento.GASTO, "180", null)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("categoría");
    }

    @Test
    void elMontoDebeSerPositivo() {
        assertThatThrownBy(() -> Movimiento.registrar(datos(TipoMovimiento.INGRESO, "0", null)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void eliminarEsLogicoYNoSePuedeRepetir() {
        var m = Movimiento.registrar(datos(TipoMovimiento.GASTO, "180", UUID.randomUUID()));

        var evento = m.eliminar(Instant.parse("2026-09-20T10:00:00Z"));

        assertThat(m.estaEliminado()).isTrue();
        assertThat(evento.tipo()).isEqualTo("movimiento.eliminado");
        assertThatThrownBy(() -> m.eliminar(Instant.now())).isInstanceOf(DomainException.class);
    }
}
