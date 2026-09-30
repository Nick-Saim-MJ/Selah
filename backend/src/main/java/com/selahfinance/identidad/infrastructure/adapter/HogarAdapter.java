package com.selahfinance.identidad.infrastructure.adapter;

import com.selahfinance.hogar.application.port.in.ConsultarHogarUseCase;
import com.selahfinance.hogar.application.port.in.CrearHogarUseCase;
import com.selahfinance.identidad.application.port.out.HogarPort;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Traduce el puerto de identidad a la API pública (port.in) del módulo hogar.
 * Si hogar se convirtiera en un servicio aparte, solo cambia este adaptador (p. ej. a un cliente HTTP).
 */
@Component
@RequiredArgsConstructor
class HogarAdapter implements HogarPort {

    private final CrearHogarUseCase crearHogar;
    private final ConsultarHogarUseCase consultarHogar;

    @Override
    public UUID crearHogarInicial(UUID usuarioId, String nombreUsuario, String nombreHogar, String moneda) {
        return crearHogar.crearIndividual(new CrearHogarUseCase.Comando(usuarioId, nombreUsuario, nombreHogar, moneda));
    }

    @Override
    public Optional<UUID> hogarPrincipalDe(UUID usuarioId) {
        return consultarHogar.hogarPrincipalDe(usuarioId);
    }
}
