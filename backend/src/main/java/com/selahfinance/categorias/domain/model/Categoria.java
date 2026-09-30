package com.selahfinance.categorias.domain.model;

import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/** Categoría editable por el hogar, con su presupuesto mensual por defecto. */
public record Categoria(
        UUID id,
        UUID hogarId,
        String nombre,
        TipoCategoria tipo,
        String color,
        String icono,
        Dinero presupuestoMensual,
        int orden,
        boolean activa) {

    private static final Pattern COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    /** Las 7 sugeridas en el asistente inicial (spec 4.2 paso 4) con los colores del prototipo. */
    public static final List<Sugerida> SUGERIDAS = List.of(
            new Sugerida("Vivienda", "#5E7BAA", "home"),
            new Sugerida("Alimentación", "#887B2C", "restaurant"),
            new Sugerida("Transporte", "#258B86", "directions_bus"),
            new Sugerida("Salud", "#9E658B", "favorite"),
            new Sugerida("Educación", "#816FA3", "school"),
            new Sugerida("Deudas", "#69625A", "credit_card"),
            new Sugerida("Ocio", "#B27744", "celebration"));

    public record Sugerida(String nombre, String color, String icono) {
    }

    public Categoria {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("CATEGORIA_NOMBRE_REQUERIDO", "La categoría necesita un nombre");
        }
        nombre = nombre.trim();
        if (color != null && !COLOR.matcher(color).matches()) {
            throw new DomainException("COLOR_INVALIDO", "El color debe tener formato #RRGGBB");
        }
        if (presupuestoMensual == null || presupuestoMensual.esNegativo()) {
            throw new DomainException("PRESUPUESTO_INVALIDO", "El presupuesto no puede ser negativo");
        }
    }

    public static Categoria nueva(UUID hogarId, String nombre, TipoCategoria tipo, String color, String icono,
            Dinero presupuestoMensual, int orden) {
        return new Categoria(UUID.randomUUID(), hogarId, nombre, tipo, color, icono, presupuestoMensual, orden, true);
    }

    public Categoria editar(String nombre, String color, String icono, Dinero presupuestoMensual) {
        return new Categoria(id, hogarId, nombre, tipo, color, icono, presupuestoMensual, orden, activa);
    }

    public Categoria conActiva(boolean nuevaActiva) {
        return new Categoria(id, hogarId, nombre, tipo, color, icono, presupuestoMensual, orden, nuevaActiva);
    }
}
