package com.selahfinance.hogar.application.port.in;

import java.util.UUID;

public interface CrearHogarUseCase {

    /** Crea un hogar individual, con el usuario como administrador y la configuración por defecto. */
    UUID crearIndividual(Comando comando);

    record Comando(UUID usuarioId, String nombreUsuario, String nombreHogar, String moneda) {
    }
}
