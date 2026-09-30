package com.selahfinance.hogar.application.port.in;

import java.util.Optional;
import java.util.UUID;

public interface ConsultarHogarUseCase {

    /** Hogar con el que el usuario inicia sesión (el primero al que se unió). */
    Optional<UUID> hogarPrincipalDe(UUID usuarioId);
}
