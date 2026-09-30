package com.selahfinance.hogar.application.usecase;

import com.selahfinance.hogar.application.port.in.ConsultarHogarUseCase;
import com.selahfinance.hogar.application.port.in.CrearHogarUseCase;
import com.selahfinance.hogar.application.port.out.ConfiguracionMayordomiaRepositoryPort;
import com.selahfinance.hogar.application.port.out.HogarRepositoryPort;
import com.selahfinance.hogar.domain.event.HogarCreado;
import com.selahfinance.hogar.domain.model.ConfiguracionMayordomia;
import com.selahfinance.hogar.domain.model.Hogar;
import com.selahfinance.shared.application.port.out.EventPublisherPort;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class HogarService implements CrearHogarUseCase, ConsultarHogarUseCase {

    private final HogarRepositoryPort hogares;
    private final ConfiguracionMayordomiaRepositoryPort configuraciones;
    private final EventPublisherPort eventos;
    private final Clock clock;

    @Override
    @Transactional
    public UUID crearIndividual(Comando c) {
        var hogar = Hogar.individual(c.usuarioId(), c.nombreHogar(), c.moneda());
        hogares.guardarConAdministrador(hogar, c.usuarioId(), c.nombreUsuario());
        configuraciones.guardar(ConfiguracionMayordomia.porDefecto(hogar.id()));
        // Categorías por defecto, etc.: los módulos interesados reaccionan en la misma transacción.
        eventos.publicar(new HogarCreado(hogar.id(), c.usuarioId(), clock.instant()));
        return hogar.id();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> hogarPrincipalDe(UUID usuarioId) {
        return hogares.hogarPrincipalDe(usuarioId);
    }
}
