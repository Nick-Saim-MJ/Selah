package com.selahfinance.identidad.application.port.in;

import com.selahfinance.identidad.application.dto.SesionResult;
import java.util.UUID;

public interface RegistrarUsuarioUseCase {

    /** Auto-registro público: crea un HERMANO en la iglesia elegida, con su hogar inicial. */
    SesionResult registrar(Comando comando);

    record Comando(
            String email,
            String password,
            String nombres,
            String apellidos,
            UUID iglesiaId,
            String nombreHogar,
            String moneda) {
    }
}
