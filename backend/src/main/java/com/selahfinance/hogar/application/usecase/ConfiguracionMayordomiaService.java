package com.selahfinance.hogar.application.usecase;

import com.selahfinance.hogar.application.port.in.ConfiguracionMayordomiaUseCase;
import com.selahfinance.hogar.application.port.out.ConfiguracionMayordomiaRepositoryPort;
import com.selahfinance.hogar.domain.model.ConfiguracionMayordomia;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class ConfiguracionMayordomiaService implements ConfiguracionMayordomiaUseCase {

    private final ConfiguracionMayordomiaRepositoryPort configuraciones;

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionMayordomia obtener(UUID hogarId) {
        return configuraciones.porHogar(hogarId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Configuración de mayordomía", hogarId));
    }

    @Override
    @Transactional
    public ConfiguracionMayordomia actualizar(ConfiguracionMayordomia configuracion) {
        obtener(configuracion.hogarId());
        return configuraciones.guardar(configuracion);
    }
}
