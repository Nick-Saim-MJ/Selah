package com.selahfinance.movimientos.infrastructure.adapter;

import com.selahfinance.movimientos.application.port.out.ReferenciasHogarPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Consultas de pertenencia directas por SQL. Cuando los módulos categorias/metas/deudas
 * tengan sus propios casos de uso, este adaptador puede delegar en ellos.
 */
@Component
@RequiredArgsConstructor
class ReferenciasHogarJdbcAdapter implements ReferenciasHogarPort {

    private final JdbcClient jdbc;

    @Override
    public boolean categoriaPertenece(UUID hogarId, UUID categoriaId) {
        return existe("select exists(select 1 from categoria where id = ? and hogar_id = ? and activa)", categoriaId, hogarId);
    }

    @Override
    public boolean fuenteIngresoPertenece(UUID hogarId, UUID fuenteIngresoId) {
        return existe("select exists(select 1 from fuente_ingreso where id = ? and hogar_id = ?)", fuenteIngresoId, hogarId);
    }

    @Override
    public boolean metaPertenece(UUID hogarId, UUID metaId) {
        return existe("select exists(select 1 from meta_ahorro where id = ? and hogar_id = ? and estado = 'ACTIVA')",
                metaId, hogarId);
    }

    @Override
    public boolean deudaPertenece(UUID hogarId, UUID deudaId) {
        return existe("select exists(select 1 from deuda where id = ? and hogar_id = ? and estado = 'ACTIVA')",
                deudaId, hogarId);
    }

    @Override
    public boolean destinoOfrendaDisponible(UUID hogarId, UUID destinoOfrendaId) {
        return existe("""
                select exists(select 1 from destino_ofrenda
                              where id = ? and activo and (hogar_id is null or hogar_id = ?))""",
                destinoOfrendaId, hogarId);
    }

    private boolean existe(String sql, UUID id, UUID hogarId) {
        return Boolean.TRUE.equals(jdbc.sql(sql).params(id, hogarId).query(Boolean.class).single());
    }
}
