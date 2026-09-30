package com.selahfinance.categorias.domain.model;

import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.util.UUID;

/** Origen de ingresos del hogar con su monto mensual estimado (asistente inicial, paso 2). */
public record FuenteIngreso(UUID id, UUID hogarId, String nombre, Dinero montoEstimadoMensual, boolean activa) {

    public FuenteIngreso {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("FUENTE_NOMBRE_REQUERIDO", "La fuente de ingreso necesita un nombre");
        }
        nombre = nombre.trim();
        if (montoEstimadoMensual == null || montoEstimadoMensual.esNegativo()) {
            throw new DomainException("MONTO_INVALIDO", "El monto estimado no puede ser negativo");
        }
    }

    public static FuenteIngreso nueva(UUID hogarId, String nombre, Dinero montoEstimadoMensual) {
        return new FuenteIngreso(UUID.randomUUID(), hogarId, nombre, montoEstimadoMensual, true);
    }

    public FuenteIngreso editar(String nombre, Dinero montoEstimadoMensual) {
        return new FuenteIngreso(id, hogarId, nombre, montoEstimadoMensual, activa);
    }

    public FuenteIngreso conActiva(boolean nuevaActiva) {
        return new FuenteIngreso(id, hogarId, nombre, montoEstimadoMensual, nuevaActiva);
    }
}
