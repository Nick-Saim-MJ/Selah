package com.selahfinance.metas.infrastructure.adapter;

import com.selahfinance.metas.application.port.out.AportesMetaPort;
import com.selahfinance.shared.domain.Dinero;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Lee la fuente única de dinero (movimiento). No hay tabla propia de "monto actual". */
@Component
@RequiredArgsConstructor
class AportesMetaJdbcAdapter implements AportesMetaPort {

    private final JdbcClient jdbc;

    @Override
    public Map<UUID, Dinero> montosAportados(Collection<UUID> metaIds) {
        if (metaIds.isEmpty()) {
            return Map.of();
        }
        var resultado = new HashMap<UUID, Dinero>();
        jdbc.sql("""
                        select meta_id, sum(monto)
                        from movimiento
                        where tipo = 'APORTE_META' and deleted_at is null and meta_id in (:ids)
                        group by meta_id""")
                .param("ids", metaIds)
                .query((rs, n) -> resultado.put(rs.getObject(1, UUID.class), Dinero.de(rs.getBigDecimal(2))))
                .list();
        return resultado;
    }
}
