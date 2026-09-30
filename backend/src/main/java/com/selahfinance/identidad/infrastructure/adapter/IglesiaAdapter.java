package com.selahfinance.identidad.infrastructure.adapter;

import com.selahfinance.iglesias.application.port.in.ConsultarIglesiasUseCase;
import com.selahfinance.identidad.application.port.out.IglesiaPort;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class IglesiaAdapter implements IglesiaPort {

    private final ConsultarIglesiasUseCase iglesias;

    @Override
    public boolean existeActiva(UUID iglesiaId) {
        return iglesias.existeActiva(iglesiaId);
    }

    @Override
    public Map<UUID, String> nombres(Collection<UUID> iglesiaIds) {
        return iglesias.nombres(iglesiaIds);
    }
}
