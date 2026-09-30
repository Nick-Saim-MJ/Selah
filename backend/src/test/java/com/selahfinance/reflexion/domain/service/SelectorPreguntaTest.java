package com.selahfinance.reflexion.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.selahfinance.reflexion.domain.model.PreguntaReflexion;
import com.selahfinance.shared.domain.DomainException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class SelectorPreguntaTest {

    private static final List<PreguntaReflexion> PREGUNTAS = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> new PreguntaReflexion((short) i, "Pregunta " + i, null, null)).toList();

    @Test
    void laMismaSemanaDaLaMismaPreguntaYLaSiguienteOtra() {
        var semana = LocalDate.of(2026, 9, 27);

        var a = SelectorPregunta.paraSemana(PREGUNTAS, semana);

        assertThat(SelectorPregunta.paraSemana(PREGUNTAS, semana)).isEqualTo(a);
        assertThat(SelectorPregunta.paraSemana(PREGUNTAS, semana.plusWeeks(1))).isNotEqualTo(a);
    }

    @Test
    void rotaPorTodoElBancoAntesDeRepetir() {
        var inicio = LocalDate.of(2026, 9, 27);

        long distintas = IntStream.range(0, 10).mapToObj(i -> SelectorPregunta.paraSemana(PREGUNTAS, inicio.plusWeeks(i)))
                .distinct().count();

        assertThat(distintas).isEqualTo(10);
    }

    @Test
    void sinPreguntasFalla() {
        assertThatThrownBy(() -> SelectorPregunta.paraSemana(List.of(), LocalDate.now())).isInstanceOf(DomainException.class);
    }
}
