package com.selahfinance.notificaciones.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.selahfinance.notificaciones.domain.model.CategoriaNotificacion;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class PoliticaEntregaTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final ZonedDateTime VIERNES_NOCHE = ZonedDateTime.of(2026, 10, 2, 20, 0, 0, 0, LIMA);
    private static final ZonedDateTime SABADO_ATARDECER = ZonedDateTime.of(2026, 10, 3, 18, 0, 0, 0, LIMA);
    private static final ZonedDateTime LUNES = ZonedDateTime.of(2026, 10, 5, 10, 0, 0, 0, LIMA);

    @Test
    void losAvisosDeConsumoSePosponenAlTerminarElSabado() {
        var programada = PoliticaEntrega.programar(CategoriaNotificacion.CONSUMO, VIERNES_NOCHE, true);

        assertThat(programada).isEqualTo(SABADO_ATARDECER.toInstant());
    }

    @Test
    void elDiezmoYLaReflexionNuncaSePosponen() {
        assertThat(PoliticaEntrega.programar(CategoriaNotificacion.MAYORDOMIA, VIERNES_NOCHE, true))
                .isEqualTo(VIERNES_NOCHE.toInstant());
    }

    @Test
    void sinModoSabadoOFueraDeLaVentanaSeEntregaAhora() {
        assertThat(PoliticaEntrega.programar(CategoriaNotificacion.CONSUMO, VIERNES_NOCHE, false))
                .isEqualTo(VIERNES_NOCHE.toInstant());
        assertThat(PoliticaEntrega.programar(CategoriaNotificacion.CONSUMO, LUNES, true)).isEqualTo(LUNES.toInstant());
    }
}
