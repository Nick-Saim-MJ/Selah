package com.selahfinance.mayordomia.infrastructure.adapter;

import com.selahfinance.hogar.application.port.in.ConfiguracionMayordomiaUseCase;
import com.selahfinance.mayordomia.application.port.out.PoliticaMayordomiaPort;
import com.selahfinance.mayordomia.domain.model.PoliticaMayordomia;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PoliticaMayordomiaAdapter implements PoliticaMayordomiaPort {

    private final ConfiguracionMayordomiaUseCase configuracion;

    @Override
    public PoliticaMayordomia de(UUID hogarId) {
        var c = configuracion.obtener(hogarId);
        return new PoliticaMayordomia(c.pctDiezmo(), c.pctOfrendaEfectivo());
    }
}
