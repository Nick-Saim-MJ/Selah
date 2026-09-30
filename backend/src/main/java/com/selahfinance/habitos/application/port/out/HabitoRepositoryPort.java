package com.selahfinance.habitos.application.port.out;

import com.selahfinance.habitos.domain.model.Habito;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface HabitoRepositoryPort {

    /** Hábitos activos del sistema más los propios del hogar (hogarId puede ser null). */
    List<Habito> activos(UUID hogarId);

    Optional<Habito> porCodigo(UUID hogarId, String codigo);

    /** Suma de lo registrado por el propio usuario (fuente APP) en el rango, por hábito. */
    Map<UUID, BigDecimal> sumasPorHabito(UUID usuarioId, LocalDate desde, LocalDate hasta);

    /** Suma de un día concreto por hábito. */
    Map<UUID, BigDecimal> sumasDelDia(UUID usuarioId, LocalDate fecha);

    /** Crea o reemplaza el registro (usuario, hábito, fecha, fuente APP). */
    void guardarRegistro(UUID usuarioId, UUID habitoId, LocalDate fecha, BigDecimal valor, String nota);
}
