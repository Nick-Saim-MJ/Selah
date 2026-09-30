package com.selahfinance.notificaciones.application.port.out;

import com.selahfinance.notificaciones.domain.model.Notificacion;
import com.selahfinance.notificaciones.domain.model.PreferenciasNotificacion;
import com.selahfinance.shared.application.Pagina;
import com.selahfinance.shared.domain.Dinero;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puertos de salida del módulo notificaciones. */
public final class NotificacionesPorts {

    private NotificacionesPorts() {
    }

    public interface NotificacionRepositoryPort {

        Notificacion guardar(Notificacion notificacion);

        boolean existeClave(UUID usuarioId, String claveDedupe);

        Pagina<Notificacion> visibles(UUID usuarioId, Instant ahora, int pagina, int tamanio);

        long contarNoLeidasVisibles(UUID usuarioId, Instant ahora);

        Optional<Notificacion> porId(UUID usuarioId, UUID notificacionId);

        void marcarTodasLeidas(UUID usuarioId, Instant ahora);
    }

    public interface PreferenciasRepositoryPort {

        Optional<PreferenciasNotificacion> de(UUID usuarioId);

        PreferenciasNotificacion guardar(PreferenciasNotificacion preferencias);
    }

    /** ¿El hogar tiene activo el Modo Sábado? (configuración del módulo hogar) */
    public interface PoliticaSabadoPort {

        boolean modoSabadoActivo(UUID hogarId);
    }

    /** Consultas de datos ajenos que necesitan las tareas programadas. */
    public interface DatosAvisosPort {

        /** Diezmo pendiente del mes cuyo día de entrega ya llegó, por cada usuario del hogar. */
        List<DiezmoPendiente> diezmosPendientes(LocalDate primerDiaDelMes, int diaDelMes);

        List<CuotaPorVencer> cuotasQueVencenElDia(int diaDelMes);

        /** Usuarios con hogar: hermanos y pastores activos. */
        List<CuentaConHogar> cuentasConHogar();

        ResumenSemanal resumenSemanal(UUID hogarId, LocalDate desde, LocalDate hasta);

        /** Plan y gasto real de una categoría en el mes de {@code fecha}; vacío si no existe. */
        Optional<EstadoPresupuesto> estadoPresupuesto(UUID hogarId, UUID categoriaId, LocalDate fecha);
    }

    public record DiezmoPendiente(UUID usuarioId, UUID hogarId, Dinero monto) {
    }

    public record CuotaPorVencer(UUID usuarioId, UUID hogarId, UUID deudaId, String nombre, Dinero cuota) {
    }

    public record CuentaConHogar(UUID usuarioId, UUID hogarId) {
    }

    public record ResumenSemanal(Dinero entro, Dinero gasto) {
    }

    public record EstadoPresupuesto(String categoria, Dinero planeado, Dinero real) {
    }
}
