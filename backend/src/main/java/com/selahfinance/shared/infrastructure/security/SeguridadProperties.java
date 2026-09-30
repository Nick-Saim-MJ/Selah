package com.selahfinance.shared.infrastructure.security;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propiedades {@code selah.seguridad.*} (ver application.yml). */
@ConfigurationProperties(prefix = "selah.seguridad")
public record SeguridadProperties(
        String jwtSecret,
        Duration jwtExpiracion,
        String jwtEmisor,
        List<String> corsOrigenes) {
}
