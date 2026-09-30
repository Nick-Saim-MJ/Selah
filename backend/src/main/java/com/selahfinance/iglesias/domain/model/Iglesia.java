package com.selahfinance.iglesias.domain.model;

import com.selahfinance.shared.domain.DomainException;
import java.util.UUID;

public record Iglesia(UUID id, String nombre, String ciudad, String distrito, boolean activa) {

    public Iglesia {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("IGLESIA_NOMBRE_REQUERIDO", "La iglesia necesita un nombre");
        }
        nombre = nombre.trim();
        ciudad = ciudad == null || ciudad.isBlank() ? null : ciudad.trim();
        distrito = distrito == null || distrito.isBlank() ? null : distrito.trim();
    }

    public static Iglesia nueva(String nombre, String ciudad, String distrito) {
        return new Iglesia(UUID.randomUUID(), nombre, ciudad, distrito, true);
    }

    public Iglesia actualizar(String nombre, String ciudad, String distrito, boolean activa) {
        return new Iglesia(id, nombre, ciudad, distrito, activa);
    }
}
