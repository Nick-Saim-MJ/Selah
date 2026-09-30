package com.selahfinance.identidad.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** Lo que identidad necesita del módulo hogar (implementado por un adaptador, no por hogar directamente). */
public interface HogarPort {

    UUID crearHogarInicial(UUID usuarioId, String nombreUsuario, String nombreHogar, String moneda);

    Optional<UUID> hogarPrincipalDe(UUID usuarioId);
}
