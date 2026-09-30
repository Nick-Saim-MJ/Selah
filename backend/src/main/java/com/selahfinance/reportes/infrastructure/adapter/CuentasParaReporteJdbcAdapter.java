package com.selahfinance.reportes.infrastructure.adapter;

import com.selahfinance.reportes.application.port.out.CuentasParaReportePort;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CuentasParaReporteJdbcAdapter implements CuentasParaReportePort {

    private final JdbcClient jdbc;

    @Override
    public List<Cuenta> activas() {
        return jdbc.sql("""
                        select u.id, min(mh.hogar_id::text)::uuid
                        from usuario u
                        join miembro_hogar mh on mh.usuario_id = u.id and mh.activo
                        where u.estado = 'ACTIVO' and u.rol in ('HERMANO', 'PASTOR')
                        group by u.id""")
                .query((rs, n) -> new Cuenta(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class)))
                .list();
    }
}
