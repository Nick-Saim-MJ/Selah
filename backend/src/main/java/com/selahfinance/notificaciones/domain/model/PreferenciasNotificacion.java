package com.selahfinance.notificaciones.domain.model;

import java.util.UUID;

/** Qué avisos quiere recibir el usuario (spec 4.8). Todo activo por defecto. */
public record PreferenciasNotificacion(
        UUID usuarioId,
        boolean recordatorioDiezmo,
        boolean recordatorioDeudas,
        boolean resumenSemanal,
        boolean reflexionSabado) {

    public static PreferenciasNotificacion porDefecto(UUID usuarioId) {
        return new PreferenciasNotificacion(usuarioId, true, true, true, true);
    }

    public boolean permite(TipoNotificacion tipo) {
        return switch (tipo) {
            case DIEZMO_PENDIENTE -> recordatorioDiezmo;
            case CUOTA_DEUDA -> recordatorioDeudas;
            case RESUMEN_SEMANAL -> resumenSemanal;
            case REFLEXION_SABADO -> reflexionSabado;
            case PRESUPUESTO_EXCEDIDO -> true;
        };
    }
}
