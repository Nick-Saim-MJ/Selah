package com.selahfinance.identidad.application.port.in;

import com.selahfinance.identidad.domain.model.Usuario;
import java.util.Optional;

/** Alta del primer administrador al arrancar el sistema (nadie más puede crear el primero). */
public interface CrearAdminInicialUseCase {

    /**
     * Crea un ADMIN si ese email aún no está registrado. Con {@code forzarCambio} la contraseña
     * debe cambiarse en el primer ingreso (recomendado fuera de las demos).
     */
    Optional<Usuario> crearSiNoExiste(String email, String password, String nombres, boolean forzarCambio);
}
