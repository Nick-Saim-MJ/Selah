package com.selahfinance.hogar.application.port.in;

import com.selahfinance.hogar.domain.model.ConfiguracionMayordomia;
import java.util.UUID;

public interface ConfiguracionMayordomiaUseCase {

    ConfiguracionMayordomia obtener(UUID hogarId);

    ConfiguracionMayordomia actualizar(ConfiguracionMayordomia configuracion);
}
