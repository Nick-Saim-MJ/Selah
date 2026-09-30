package com.selahfinance.habitos.application.port.in;

import com.selahfinance.habitos.domain.model.HabitoDelDia;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GestionarHabitosUseCase {

    /** Todos los hábitos activos con lo registrado hoy y en la semana (domingo a sábado). */
    List<HabitoDelDia> delDia(UUID usuarioId, UUID hogarId, LocalDate fecha);

    /** Registra (o corrige) el valor de un hábito en un día. Un registro por hábito y día. */
    HabitoDelDia registrar(UUID usuarioId, UUID hogarId, String codigo, LocalDate fecha, BigDecimal valor, String nota);
}
