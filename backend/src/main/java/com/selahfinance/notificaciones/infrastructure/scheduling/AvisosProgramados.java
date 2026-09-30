package com.selahfinance.notificaciones.infrastructure.scheduling;

import com.selahfinance.notificaciones.application.port.in.GenerarAvisosUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Horarios en la zona de la app (America/Lima por defecto). */
@Slf4j
@Component
@RequiredArgsConstructor
class AvisosProgramados {

    private static final String ZONA = "${selah.zona-horaria:America/Lima}";

    private final GenerarAvisosUseCase avisos;

    /** Cada mañana: diezmo pendiente y cuotas que vencen mañana. */
    @Scheduled(cron = "0 0 9 * * *", zone = ZONA)
    void avisosDiarios() {
        int diezmos = avisos.recordarDiezmosPendientes();
        int cuotas = avisos.recordarCuotasDeDeuda();
        log.info("Avisos diarios: {} de diezmo, {} de cuotas", diezmos, cuotas);
    }

    /** Viernes 16:00: invitación a la reflexión (la tarjeta aparece en Inicio desde las 15:00). */
    @Scheduled(cron = "0 0 16 * * FRI", zone = ZONA)
    void reflexionDelViernes() {
        log.info("Invitaciones a la reflexión: {}", avisos.invitarAReflexion());
    }

    /** Domingo 19:00: resumen de la semana. */
    @Scheduled(cron = "0 0 19 * * SUN", zone = ZONA)
    void resumenDelDomingo() {
        log.info("Resúmenes semanales: {}", avisos.enviarResumenSemanal());
    }
}
