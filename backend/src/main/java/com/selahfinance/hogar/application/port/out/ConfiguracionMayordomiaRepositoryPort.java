package com.selahfinance.hogar.application.port.out;

import com.selahfinance.hogar.domain.model.ConfiguracionMayordomia;
import java.util.Optional;
import java.util.UUID;

public interface ConfiguracionMayordomiaRepositoryPort {

    Optional<ConfiguracionMayordomia> porHogar(UUID hogarId);

    ConfiguracionMayordomia guardar(ConfiguracionMayordomia configuracion);
}
