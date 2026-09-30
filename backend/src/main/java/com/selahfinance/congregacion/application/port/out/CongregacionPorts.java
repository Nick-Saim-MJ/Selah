package com.selahfinance.congregacion.application.port.out;

import com.selahfinance.reportes.domain.model.ReporteMayordomia;
import com.selahfinance.shared.domain.Periodo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puertos de salida del módulo congregación. */
public final class CongregacionPorts {

    private CongregacionPorts() {
    }

    /** Devuelve la iglesia del pastor, o lanza AccesoDenegado si la cuenta no es un pastor activo. */
    public interface PastorPort {

        UUID iglesiaDelPastor(UUID pastorId);
    }

    public interface MiembrosIglesiaPort {

        /** Hermanos activos de la iglesia que tienen hogar. */
        List<Miembro> hermanosActivos(UUID iglesiaId);
    }

    public interface ReportesMiembrosPort {

        Optional<ReporteMayordomia> existente(UUID usuarioId, UUID hogarId, Periodo periodo);

        ReporteMayordomia generar(UUID usuarioId, UUID hogarId, Periodo periodo);
    }

    public record Miembro(UUID usuarioId, String nombre, UUID hogarId, boolean comparteReporte) {
    }
}
