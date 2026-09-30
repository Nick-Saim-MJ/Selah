package com.selahfinance.config;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

/**
 * Reloj inyectable: los casos de uso nunca llaman a Instant.now() directamente.
 *
 * <p>Su zona es la de la app ({@code selah.zona-horaria}, por defecto America/Lima): así
 * {@code LocalDate.now(clock)} es el día que vive el usuario y no el de UTC (a las 19:00 de Lima
 * UTC ya está en el día siguiente, lo que descuadraría meses, el sábado y la reflexión).
 *
 * <p>Solo en los perfiles {@code dev}/{@code demo}, {@code SELAH_DEMO_RELOJ_SIMULADO=2026-10-02T21:00:00Z}
 * adelanta el reloj a ese instante (y sigue corriendo). Sirve para mostrar el Modo Sábado y la
 * reflexión semanal en una presentación hecha un día de semana.
 */
@Slf4j
@Configuration
public class ClockConfig {

    @Bean
    Clock clock(@Value("${selah.zona-horaria:America/Lima}") String zona,
            @Value("${selah.demo.reloj-simulado:}") String simulado, Environment env) {
        Clock real = Clock.system(ZoneId.of(zona));
        if (simulado.isBlank()) {
            return real;
        }
        if (!env.acceptsProfiles(Profiles.of("dev", "demo"))) {
            throw new IllegalStateException("selah.demo.reloj-simulado solo se permite en los perfiles dev o demo");
        }
        Duration desfase = Duration.between(Instant.now(), Instant.parse(simulado));
        log.warn("RELOJ SIMULADO activo: la hora del sistema arranca en {}", simulado);
        return Clock.offset(real, desfase);
    }
}
