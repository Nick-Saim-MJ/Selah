package com.selahfinance.habitos.application.usecase;

import com.selahfinance.habitos.application.port.in.GestionarHabitosUseCase;
import com.selahfinance.habitos.application.port.out.HabitoRepositoryPort;
import com.selahfinance.habitos.domain.model.Habito;
import com.selahfinance.habitos.domain.model.HabitoDelDia;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import com.selahfinance.shared.domain.CalendarioSabado;
import com.selahfinance.shared.domain.DomainException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class HabitosService implements GestionarHabitosUseCase {

    private final HabitoRepositoryPort habitos;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<HabitoDelDia> delDia(UUID usuarioId, UUID hogarId, LocalDate fecha) {
        var dia = fecha == null ? LocalDate.now(clock) : fecha;
        var inicioSemana = CalendarioSabado.inicioDeSemana(dia);
        var hoy = habitos.sumasDelDia(usuarioId, dia);
        var semana = habitos.sumasPorHabito(usuarioId, inicioSemana, inicioSemana.plusDays(6));
        return habitos.activos(hogarId).stream()
                .map(h -> armar(h, hoy, semana))
                .toList();
    }

    @Override
    @Transactional
    public HabitoDelDia registrar(UUID usuarioId, UUID hogarId, String codigo, LocalDate fecha, BigDecimal valor,
            String nota) {
        var dia = fecha == null ? LocalDate.now(clock) : fecha;
        if (dia.isAfter(LocalDate.now(clock))) {
            throw new DomainException("FECHA_FUTURA", "No se puede registrar un hábito de una fecha futura");
        }
        var habito = habitos.porCodigo(hogarId, codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Hábito", codigo));
        habito.validarValor(valor);
        habitos.guardarRegistro(usuarioId, habito.id(), dia, valor, nota == null || nota.isBlank() ? null : nota.trim());

        var inicioSemana = CalendarioSabado.inicioDeSemana(dia);
        return armar(habito, habitos.sumasDelDia(usuarioId, dia),
                habitos.sumasPorHabito(usuarioId, inicioSemana, inicioSemana.plusDays(6)));
    }

    private static HabitoDelDia armar(Habito h, Map<UUID, BigDecimal> hoy, Map<UUID, BigDecimal> semana) {
        return new HabitoDelDia(h, hoy.getOrDefault(h.id(), BigDecimal.ZERO), semana.getOrDefault(h.id(), BigDecimal.ZERO));
    }
}
