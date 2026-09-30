package com.selahfinance.integraciones.infrastructure.web;

import com.selahfinance.integraciones.application.port.in.AutenticarClienteApiUseCase.ClienteAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Punto de entrada para sistemas de otros equipos (auth: header X-API-Key).
 * Aquí se agregan los endpoints que ellos consuman, p. ej. registrar hábitos de Templo/Talento.
 */
@Tag(name = "Integraciones")
@RestController
@RequestMapping("/api/v1/integraciones")
@RequiredArgsConstructor
class IntegracionesController {

    private final Clock clock;

    @Operation(summary = "Verifica una API key y devuelve el cliente y sus scopes")
    @GetMapping("/ping")
    PingResponse ping(@AuthenticationPrincipal ClienteAutenticado cliente) {
        return new PingResponse(cliente.nombre(), cliente.scopes(), clock.instant());
    }

    record PingResponse(String cliente, Set<String> scopes, Instant hora) {
    }
}
