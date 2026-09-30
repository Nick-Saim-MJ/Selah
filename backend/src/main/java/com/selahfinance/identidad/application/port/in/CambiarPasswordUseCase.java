package com.selahfinance.identidad.application.port.in;

import java.util.UUID;

public interface CambiarPasswordUseCase {

    void cambiar(UUID usuarioId, String passwordActual, String passwordNueva);
}
