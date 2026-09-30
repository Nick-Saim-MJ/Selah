package com.selahfinance.shared.application.exception;

/** El recurso no existe o no pertenece al hogar del usuario. Se traduce a HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " no encontrado: " + id);
    }
}
