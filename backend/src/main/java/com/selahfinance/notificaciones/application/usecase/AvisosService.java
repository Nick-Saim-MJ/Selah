package com.selahfinance.notificaciones.application.usecase;

import com.selahfinance.notificaciones.application.port.in.GenerarAvisosUseCase;
import com.selahfinance.notificaciones.application.port.in.NotificarUseCase;
import com.selahfinance.notificaciones.application.port.in.NotificarUseCase.Comando;
import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.DatosAvisosPort;
import com.selahfinance.notificaciones.domain.model.CategoriaNotificacion;
import com.selahfinance.notificaciones.domain.model.TipoNotificacion;
import com.selahfinance.shared.domain.CalendarioSabado;
import com.selahfinance.shared.domain.Dinero;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Genera los avisos automáticos. Cada uno lleva una clave para no repetirse aunque la tarea corra dos veces. */
@Service
@RequiredArgsConstructor
class AvisosService implements GenerarAvisosUseCase {

    private final NotificarUseCase notificar;
    private final DatosAvisosPort datos;
    private final Clock clock;

    @Override
    @Transactional
    public int recordarDiezmosPendientes() {
        var hoy = LocalDate.now(clock);
        var mes = YearMonth.from(hoy);
        int creados = 0;
        for (var d : datos.diezmosPendientes(mes.atDay(1), hoy.getDayOfMonth())) {
            creados += contar(notificar.notificar(new Comando(d.usuarioId(), d.hogarId(),
                    TipoNotificacion.DIEZMO_PENDIENTE, CategoriaNotificacion.MAYORDOMIA,
                    "Diezmo por entregar",
                    "Tienes " + soles(d.monto()) + " de diezmo apartado este mes. Lo primero para Dios.",
                    Map.of("ruta", "/diezmos"), "diezmo:" + mes)));
        }
        return creados;
    }

    @Override
    @Transactional
    public int recordarCuotasDeDeuda() {
        var manana = LocalDate.now(clock).plusDays(1);
        int creados = 0;
        for (var c : datos.cuotasQueVencenElDia(manana.getDayOfMonth())) {
            creados += contar(notificar.notificar(new Comando(c.usuarioId(), c.hogarId(),
                    TipoNotificacion.CUOTA_DEUDA, CategoriaNotificacion.CONSUMO,
                    "Mañana vence una cuota",
                    c.nombre() + ": " + soles(c.cuota()) + ". Pagar a tiempo es honrar lo prometido.",
                    Map.of("ruta", "/metas"), "cuota:" + c.deudaId() + ":" + YearMonth.from(manana))));
        }
        return creados;
    }

    @Override
    @Transactional
    public int invitarAReflexion() {
        var semana = CalendarioSabado.inicioDeSemana(LocalDate.now(clock));
        int creados = 0;
        for (var c : datos.cuentasConHogar()) {
            creados += contar(notificar.notificar(new Comando(c.usuarioId(), c.hogarId(),
                    TipoNotificacion.REFLEXION_SABADO, CategoriaNotificacion.MAYORDOMIA,
                    "Tu reflexión de la semana te espera",
                    "Un momento para revisar, dar gracias y descansar. Sin números que registrar.",
                    Map.of("ruta", "/reflexion"), "reflexion:" + semana)));
        }
        return creados;
    }

    @Override
    @Transactional
    public int enviarResumenSemanal() {
        var hoy = LocalDate.now(clock);
        var semana = CalendarioSabado.inicioDeSemana(hoy);
        int creados = 0;
        for (var c : datos.cuentasConHogar()) {
            var r = datos.resumenSemanal(c.hogarId(), semana, hoy);
            creados += contar(notificar.notificar(new Comando(c.usuarioId(), c.hogarId(),
                    TipoNotificacion.RESUMEN_SEMANAL, CategoriaNotificacion.RECORDATORIO,
                    "Tu semana en números",
                    "Entraron " + soles(r.entro()) + " y gastaste " + soles(r.gasto()) + " esta semana.",
                    Map.of("ruta", "/inicio"), "resumen:" + semana)));
        }
        return creados;
    }

    @Override
    @Transactional
    public void alertarSiPresupuestoExcedido(UUID hogarId, UUID usuarioId, UUID categoriaId, BigDecimal monto,
            LocalDate fecha) {
        datos.estadoPresupuesto(hogarId, categoriaId, fecha).ifPresent(e -> {
            var gasto = Dinero.de(monto);
            // Solo cuando ESTE gasto es el que cruza el límite (no en cada gasto posterior)
            boolean cruzoElLimite = e.planeado().esPositivo() && e.real().compareTo(e.planeado()) > 0
                    && e.real().restar(gasto).compareTo(e.planeado()) <= 0;
            if (cruzoElLimite) {
                notificar.notificar(new Comando(usuarioId, hogarId, TipoNotificacion.PRESUPUESTO_EXCEDIDO,
                        CategoriaNotificacion.CONSUMO, "Superaste tu presupuesto de " + e.categoria(),
                        "Llevas " + soles(e.real()) + " de " + soles(e.planeado()) + " este mes.",
                        Map.of("ruta", "/reportes"), "presupuesto:" + categoriaId + ":" + YearMonth.from(fecha)));
            }
        });
    }

    private static int contar(java.util.Optional<?> creada) {
        return creada.isPresent() ? 1 : 0;
    }

    private static String soles(Dinero monto) {
        return "S/ " + monto;
    }
}
