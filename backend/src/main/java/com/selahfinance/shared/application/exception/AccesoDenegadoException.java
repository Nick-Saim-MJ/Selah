package com.selahfinance.shared.application.exception;

/** El usuario está autenticado pero no puede realizar la acción. Se traduce a HTTP 403. */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
