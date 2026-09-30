package com.selahfinance.reflexion.application.usecase;

import com.selahfinance.reflexion.application.port.in.GestionarReflexionUseCase;
import com.selahfinance.reflexion.application.port.out.ReflexionPorts.PreguntaRepositoryPort;
import com.selahfinance.reflexion.application.port.out.ReflexionPorts.RespuestaRepositoryPort;
import com.selahfinance.reflexion.application.port.out.ReflexionPorts.ResumenSemanaPort;
import com.selahfinance.reflexion.domain.model.TarjetaReflexion;
import com.selahfinance.reflexion.domain.service.SelectorPregunta;
import com.selahfinance.shared.domain.CalendarioSabado;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class ReflexionService implements GestionarReflexionUseCase {

    private static final int MAX_RESPUESTA = 2000;

    private final PreguntaRepositoryPort preguntas;
    private final RespuestaRepositoryPort respuestas;
    private final ResumenSemanaPort resumenes;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public TarjetaReflexion actual(UUID usuarioId, UUID hogarId) {
        return armar(usuarioId, hogarId);
    }

    @Override
    @Transactional
    public TarjetaReflexion responder(UUID usuarioId, UUID hogarId, String respuesta) {
        var semana = CalendarioSabado.inicioDeSemana(LocalDate.now(clock));
        var pregunta = SelectorPregunta.paraSemana(preguntas.activas(), semana);
        String limpia = respuesta == null ? "" : respuesta.trim();
        respuestas.guardar(usuarioId, semana, pregunta.id(),
                limpia.length() > MAX_RESPUESTA ? limpia.substring(0, MAX_RESPUESTA) : limpia);
        return armar(usuarioId, hogarId);
    }

    private TarjetaReflexion armar(UUID usuarioId, UUID hogarId) {
        var ahora = ZonedDateTime.now(clock);
        var hoy = ahora.toLocalDate();
        var semana = CalendarioSabado.inicioDeSemana(hoy);
        var pregunta = SelectorPregunta.paraSemana(preguntas.activas(), semana);
        var hasta = semana.plusDays(6).isAfter(hoy) ? hoy : semana.plusDays(6);
        return new TarjetaReflexion(CalendarioSabado.reflexionVisible(ahora), semana, pregunta,
                respuestas.respuesta(usuarioId, semana).filter(r -> !r.isBlank()).orElse(null),
                resumenes.resumen(hogarId, semana, hasta));
    }
}
