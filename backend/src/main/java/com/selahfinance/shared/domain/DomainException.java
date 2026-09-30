package com.selahfinance.shared.domain;

/** Violación de una regla de negocio. Se traduce a HTTP 422 en la capa web. */
public class DomainException extends RuntimeException {

    private final String codigo;

    public DomainException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
