package com.selahfinance.hogar.infrastructure.persistence;

import com.selahfinance.hogar.application.port.out.ConfiguracionMayordomiaRepositoryPort;
import com.selahfinance.hogar.application.port.out.HogarRepositoryPort;
import com.selahfinance.hogar.domain.model.ConfiguracionMayordomia;
import com.selahfinance.hogar.domain.model.Hogar;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class HogarPersistenceAdapter implements HogarRepositoryPort, ConfiguracionMayordomiaRepositoryPort {

    private final HogarJpaRepository hogares;
    private final MiembroHogarJpaRepository miembros;
    private final ConfiguracionMayordomiaJpaRepository configuraciones;

    @Override
    public void guardarConAdministrador(Hogar hogar, UUID usuarioId, String nombreMiembro) {
        var h = new HogarJpaEntity();
        h.setId(hogar.id());
        h.setNombre(hogar.nombre());
        h.setTipo(hogar.tipo().name());
        h.setMoneda(hogar.moneda());
        h.setZonaHoraria(hogar.zonaHoraria());
        h.setCreadoPor(hogar.creadoPor());
        hogares.save(h);

        var m = new MiembroHogarJpaEntity();
        m.setId(UUID.randomUUID());
        m.setHogarId(hogar.id());
        m.setUsuarioId(usuarioId);
        m.setNombre(nombreMiembro);
        m.setRol("ADMINISTRADOR");
        m.setActivo(true);
        miembros.save(m);
    }

    @Override
    public Optional<UUID> hogarPrincipalDe(UUID usuarioId) {
        return miembros.hogaresDeUsuario(usuarioId, Limit.of(1)).stream().findFirst();
    }

    @Override
    public Optional<ConfiguracionMayordomia> porHogar(UUID hogarId) {
        return configuraciones.findById(hogarId).map(HogarPersistenceAdapter::aDominio);
    }

    @Override
    public ConfiguracionMayordomia guardar(ConfiguracionMayordomia c) {
        var e = configuraciones.findById(c.hogarId()).orElseGet(ConfiguracionMayordomiaJpaEntity::new);
        e.setHogarId(c.hogarId());
        e.setPctDiezmo(c.pctDiezmo());
        e.setOfrendaActiva(c.ofrendaActiva());
        e.setPctOfrenda(c.pctOfrenda());
        e.setDiaEntregaDiezmo(c.diaEntregaDiezmo() == null ? null : c.diaEntregaDiezmo().shortValue());
        e.setModoSabadoActivo(c.modoSabadoActivo());
        return aDominio(configuraciones.save(e));
    }

    private static ConfiguracionMayordomia aDominio(ConfiguracionMayordomiaJpaEntity e) {
        return new ConfiguracionMayordomia(e.getHogarId(), e.getPctDiezmo(), e.isOfrendaActiva(), e.getPctOfrenda(),
                e.getDiaEntregaDiezmo() == null ? null : e.getDiaEntregaDiezmo().intValue(), e.isModoSabadoActivo());
    }
}
