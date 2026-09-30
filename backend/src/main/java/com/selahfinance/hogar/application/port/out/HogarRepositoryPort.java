package com.selahfinance.hogar.application.port.out;

import com.selahfinance.hogar.domain.model.Hogar;
import java.util.Optional;
import java.util.UUID;

public interface HogarRepositoryPort {

    void guardarConAdministrador(Hogar hogar, UUID usuarioId, String nombreMiembro);

    Optional<UUID> hogarPrincipalDe(UUID usuarioId);
}
