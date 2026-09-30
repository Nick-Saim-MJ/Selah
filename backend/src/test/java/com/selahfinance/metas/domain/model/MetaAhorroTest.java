package com.selahfinance.metas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MetaAhorroTest {

    private static MetaAhorro meta(String objetivo, LocalDate fecha) {
        return MetaAhorro.nueva(UUID.randomUUID(), "Fondo de emergencia", "Tres meses de gastos", TipoMeta.EMERGENCIA,
                Dinero.de(objetivo), fecha, true);
    }

    @Test
    void cadaMetaExigeUnProposito() {
        assertThatThrownBy(() -> MetaAhorro.nueva(UUID.randomUUID(), "Ahorro", " ", TipoMeta.LIBRE, Dinero.de("100"),
                null, false)).isInstanceOf(DomainException.class).hasMessageContaining("propósito");
    }

    @Test
    void progresoPorcentajeYRestanteConTopeEnCien() {
        var m = meta("6000", null);

        var medio = new MetaConProgreso(m, Dinero.de("3600"));
        assertThat(medio.porcentaje()).isEqualByComparingTo("60");
        assertThat(medio.restante()).isEqualTo(Dinero.de("2400"));
        assertThat(medio.alcanzada()).isFalse();

        var pasada = new MetaConProgreso(m, Dinero.de("7000"));
        assertThat(pasada.porcentaje()).isEqualByComparingTo("100");
        assertThat(pasada.restante()).isEqualTo(Dinero.CERO);
        assertThat(pasada.alcanzada()).isTrue();
    }

    @Test
    void aporteMensualSugeridoParaLlegarALaFecha() {
        var m = meta("6000", LocalDate.of(2026, 12, 31));
        var progreso = new MetaConProgreso(m, Dinero.de("3600"));

        // Faltan 2 400 y quedan 3 meses (oct, nov, dic contando desde septiembre)
        assertThat(progreso.aporteMensualSugerido(LocalDate.of(2026, 9, 28))).isEqualTo(Dinero.de("800"));
        assertThat(new MetaConProgreso(m, Dinero.de("6000")).aporteMensualSugerido(LocalDate.of(2026, 9, 28))).isNull();
        assertThat(new MetaConProgreso(meta("6000", null), Dinero.CERO).aporteMensualSugerido(LocalDate.of(2026, 9, 28))).isNull();
    }

    @Test
    void unaMetaCanceladaOPausadaDejaDeSerPrincipal() {
        assertThat(meta("100", null).esPrincipal()).isTrue();
        assertThat(meta("100", null).conEstado(EstadoMeta.CANCELADA).esPrincipal()).isFalse();
        assertThatThrownBy(() -> meta("100", null).conEstado(EstadoMeta.PAUSADA).conPrincipal(true))
                .isInstanceOf(DomainException.class);
    }
}
