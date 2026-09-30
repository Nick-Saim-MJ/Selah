package com.selahfinance.reflexion.application.port.out;

import com.selahfinance.reflexion.domain.model.PreguntaReflexion;
import com.selahfinance.reflexion.domain.model.ResumenSemana;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puertos de salida del módulo reflexión. */
public final class ReflexionPorts {

    private ReflexionPorts() {
    }

    public interface PreguntaRepositoryPort {

        List<PreguntaReflexion> activas();
    }

    public interface RespuestaRepositoryPort {

        Optional<String> respuesta(UUID usuarioId, LocalDate semanaInicio);

        void guardar(UUID usuarioId, LocalDate semanaInicio, short preguntaId, String respuesta);
    }

    public interface ResumenSemanaPort {

        ResumenSemana resumen(UUID hogarId, LocalDate desde, LocalDate hasta);
    }
}
