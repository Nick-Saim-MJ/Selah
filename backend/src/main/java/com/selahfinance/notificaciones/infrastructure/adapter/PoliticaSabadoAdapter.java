package com.selahfinance.notificaciones.infrastructure.adapter;

import com.selahfinance.hogar.application.port.in.ConfiguracionMayordomiaUseCase;
import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.PoliticaSabadoPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PoliticaSabadoAdapter implements PoliticaSabadoPort {

    private final ConfiguracionMayordomiaUseCase configuracion;

    @Override
    public boolean modoSabadoActivo(UUID hogarId) {
        return configuracion.obtener(hogarId).modoSabadoActivo();
    }
}
