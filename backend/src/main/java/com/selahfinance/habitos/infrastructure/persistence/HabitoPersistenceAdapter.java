package com.selahfinance.habitos.infrastructure.persistence;

import com.selahfinance.habitos.application.port.out.HabitoRepositoryPort;
import com.selahfinance.habitos.domain.model.FrecuenciaMeta;
import com.selahfinance.habitos.domain.model.Habito;
import com.selahfinance.habitos.domain.model.UnidadHabito;
import com.selahfinance.shared.domain.DimensionMayordomia;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class HabitoPersistenceAdapter implements HabitoRepositoryPort {

    private static final String FUENTE_APP = "APP";

    private final HabitoJpaRepository habitos;
    private final RegistroHabitoJpaRepository registros;

    @Override
    public List<Habito> activos(UUID hogarId) {
        return habitos.activos(hogarId).stream().map(HabitoPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<Habito> porCodigo(UUID hogarId, String codigo) {
        return habitos.porCodigo(hogarId, codigo).stream().findFirst().map(HabitoPersistenceAdapter::aDominio);
    }

    @Override
    public Map<UUID, BigDecimal> sumasPorHabito(UUID usuarioId, LocalDate desde, LocalDate hasta) {
        return registros.sumas(usuarioId, desde, hasta);
    }

    @Override
    public Map<UUID, BigDecimal> sumasDelDia(UUID usuarioId, LocalDate fecha) {
        return registros.sumas(usuarioId, fecha, fecha);
    }

    @Override
    public void guardarRegistro(UUID usuarioId, UUID habitoId, LocalDate fecha, BigDecimal valor, String nota) {
        var e = registros.findByUsuarioIdAndHabitoIdAndFechaAndFuente(usuarioId, habitoId, fecha, FUENTE_APP)
                .orElseGet(() -> {
                    var nuevo = new RegistroHabitoJpaEntity();
                    nuevo.setId(UUID.randomUUID());
                    nuevo.setUsuarioId(usuarioId);
                    nuevo.setHabitoId(habitoId);
                    nuevo.setFecha(fecha);
                    nuevo.setFuente(FUENTE_APP);
                    return nuevo;
                });
        e.setValor(valor);
        e.setNota(nota);
        registros.saveAndFlush(e);
    }

    private static Habito aDominio(HabitoJpaEntity e) {
        return new Habito(e.getId(), e.getCodigo(), DimensionMayordomia.valueOf(e.getDimension()), e.getNombre(),
                e.getDescripcion(), UnidadHabito.valueOf(e.getUnidad()), FrecuenciaMeta.valueOf(e.getFrecuenciaMeta()),
                e.getMetaValor(), e.getReferenciaBiblica(), e.getOrden());
    }
}
