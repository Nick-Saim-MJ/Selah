package com.selahfinance.mayordomia.infrastructure.adapter;

import com.selahfinance.mayordomia.application.port.out.RegistroEntregaPort;
import com.selahfinance.mayordomia.domain.model.TipoApartado;
import com.selahfinance.movimientos.application.port.in.RegistrarMovimientoUseCase;
import com.selahfinance.movimientos.domain.model.OrigenMovimiento;
import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import com.selahfinance.shared.domain.Dinero;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RegistroEntregaAdapter implements RegistroEntregaPort {

    private final RegistrarMovimientoUseCase registrarMovimiento;

    @Override
    public UUID registrarEntrega(UUID hogarId, UUID usuarioId, TipoApartado tipo, Dinero monto, LocalDate fecha,
            String descripcion) {
        var tipoMovimiento = tipo == TipoApartado.DIEZMO ? TipoMovimiento.DIEZMO : TipoMovimiento.OFRENDA;
        return registrarMovimiento.registrar(new RegistrarMovimientoUseCase.Comando(
                hogarId, usuarioId, tipoMovimiento, monto.valor(), fecha, descripcion, null,
                null, null, null, null, null, true, OrigenMovimiento.APP, null)).id();
    }
}
