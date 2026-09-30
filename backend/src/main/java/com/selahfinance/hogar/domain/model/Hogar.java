package com.selahfinance.hogar.domain.model;

import com.selahfinance.shared.domain.DomainException;
import java.util.UUID;

public record Hogar(UUID id, String nombre, TipoHogar tipo, String moneda, String zonaHoraria, UUID creadoPor) {

    public static final String MONEDA_POR_DEFECTO = "PEN";
    public static final String ZONA_POR_DEFECTO = "America/Lima";

    public Hogar {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("HOGAR_NOMBRE_REQUERIDO", "El hogar necesita un nombre");
        }
        if (moneda == null || !moneda.matches("^[A-Z]{3}$")) {
            throw new DomainException("MONEDA_INVALIDA", "La moneda debe ser un código ISO 4217 (p. ej. PEN)");
        }
    }

    public static Hogar individual(UUID usuarioId, String nombre, String moneda) {
        return new Hogar(UUID.randomUUID(), nombre, TipoHogar.INDIVIDUAL,
                moneda == null ? MONEDA_POR_DEFECTO : moneda, ZONA_POR_DEFECTO, usuarioId);
    }
}
