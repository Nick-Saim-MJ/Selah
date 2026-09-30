package com.selahfinance.reflexion.domain.service;

import com.selahfinance.reflexion.domain.model.PreguntaReflexion;
import com.selahfinance.shared.domain.DomainException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Rota las preguntas: la misma para todos durante una semana y distinta la semana siguiente. */
public final class SelectorPregunta {

    private SelectorPregunta() {
    }

    public static PreguntaReflexion paraSemana(List<PreguntaReflexion> activas, LocalDate inicioSemana) {
        if (activas.isEmpty()) {
            throw new DomainException("SIN_PREGUNTAS", "No hay preguntas de reflexión activas");
        }
        var ordenadas = activas.stream().sorted(Comparator.comparing(PreguntaReflexion::id)).toList();
        long semanas = inicioSemana.toEpochDay() / 7;
        return ordenadas.get((int) Math.floorMod(semanas, (long) ordenadas.size()));
    }
}
