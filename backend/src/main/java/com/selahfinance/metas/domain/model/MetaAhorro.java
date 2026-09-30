package com.selahfinance.metas.domain.model;

import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Meta de ahorro con propósito nombrado (Prov. 6:6-8 previsión; 1 Tim. 6:6-8 contentamiento:
 * no se ahorra "por ahorrar"). El monto actual no vive aquí: se deriva de los aportes.
 */
public record MetaAhorro(
        UUID id,
        UUID hogarId,
        String nombre,
        String proposito,
        TipoMeta tipo,
        Dinero montoObjetivo,
        LocalDate fechaObjetivo,
        boolean esPrincipal,
        EstadoMeta estado) {

    public MetaAhorro {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("META_NOMBRE_REQUERIDO", "La meta necesita un nombre");
        }
        if (proposito == null || proposito.isBlank()) {
            throw new DomainException("META_PROPOSITO_REQUERIDO", "Cada meta necesita un propósito: ¿para qué ahorras?");
        }
        if (montoObjetivo == null || !montoObjetivo.esPositivo()) {
            throw new DomainException("META_OBJETIVO_INVALIDO", "El monto objetivo debe ser mayor que cero");
        }
        nombre = nombre.trim();
        proposito = proposito.trim();
    }

    public static MetaAhorro nueva(UUID hogarId, String nombre, String proposito, TipoMeta tipo, Dinero montoObjetivo,
            LocalDate fechaObjetivo, boolean esPrincipal) {
        return new MetaAhorro(UUID.randomUUID(), hogarId, nombre, proposito, tipo == null ? TipoMeta.ESPECIFICA : tipo,
                montoObjetivo, fechaObjetivo, esPrincipal, EstadoMeta.ACTIVA);
    }

    public MetaAhorro editar(String nombre, String proposito, TipoMeta tipo, Dinero montoObjetivo, LocalDate fechaObjetivo) {
        return new MetaAhorro(id, hogarId, nombre, proposito, tipo == null ? this.tipo : tipo, montoObjetivo,
                fechaObjetivo, esPrincipal, estado);
    }

    public MetaAhorro conPrincipal(boolean principal) {
        if (principal && estado != EstadoMeta.ACTIVA) {
            throw new DomainException("META_NO_ACTIVA", "Solo una meta activa puede ser la principal");
        }
        return new MetaAhorro(id, hogarId, nombre, proposito, tipo, montoObjetivo, fechaObjetivo, principal, estado);
    }

    public MetaAhorro conEstado(EstadoMeta nuevo) {
        // Una meta que deja de estar activa deja de ser la principal (índice único parcial en la BD)
        return new MetaAhorro(id, hogarId, nombre, proposito, tipo, montoObjetivo, fechaObjetivo,
                nuevo == EstadoMeta.ACTIVA && esPrincipal, nuevo);
    }

    public boolean estaActiva() {
        return estado == EstadoMeta.ACTIVA;
    }
}
