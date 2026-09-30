package com.selahfinance.deudas.infrastructure.adapter;

import com.selahfinance.categorias.application.port.in.GestionarFuentesIngresoUseCase;
import com.selahfinance.deudas.application.port.out.IngresoMensualPort;
import com.selahfinance.shared.domain.Dinero;
import java.time.YearMonth;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Ingreso estimado que el hogar declaró en el asistente inicial; si no declaró ninguno,
 * lo realmente ingresado en el mes.
 */
@Component
@RequiredArgsConstructor
class IngresoMensualAdapter implements IngresoMensualPort {

    private final GestionarFuentesIngresoUseCase fuentes;
    private final JdbcClient jdbc;

    @Override
    public Dinero ingresoBase(UUID hogarId, YearMonth mes) {
        Dinero estimado = fuentes.ingresoMensualEstimado(hogarId);
        if (estimado.esPositivo()) {
            return estimado;
        }
        return Dinero.de(jdbc.sql("""
                        select coalesce(sum(monto), 0) from movimiento
                        where hogar_id = ? and tipo = 'INGRESO' and deleted_at is null
                          and fecha between ? and ?""")
                .params(hogarId, mes.atDay(1), mes.atEndOfMonth())
                .query(java.math.BigDecimal.class)
                .single());
    }
}
