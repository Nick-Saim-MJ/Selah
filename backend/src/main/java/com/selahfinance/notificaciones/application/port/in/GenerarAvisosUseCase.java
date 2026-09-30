package com.selahfinance.notificaciones.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Generadores de avisos: los llaman las tareas programadas y los eventos de movimientos. */
public interface GenerarAvisosUseCase {

    /** Diezmo por entregar cuando ya pasó el día que el hogar eligió. Devuelve cuántos avisos creó. */
    int recordarDiezmosPendientes();

    /** Cuotas de deuda que vencen mañana. */
    int recordarCuotasDeDeuda();

    /** Viernes por la tarde: invita a la reflexión de la semana. */
    int invitarAReflexion();

    /** Domingo: cuánto entró y cuánto se gastó en la semana. */
    int enviarResumenSemanal();

    /** Tras un gasto: avisa una sola vez por mes cuando la categoría supera su presupuesto. */
    void alertarSiPresupuestoExcedido(UUID hogarId, UUID usuarioId, UUID categoriaId, BigDecimal monto, LocalDate fecha);
}
