package com.selahfinance.reportes.infrastructure.adapter;

import com.selahfinance.reportes.application.port.out.DatosFinancierosPort;
import com.selahfinance.reportes.domain.model.IndicadoresTesoro;
import com.selahfinance.reportes.domain.model.LineaPresupuesto;
import com.selahfinance.reportes.domain.model.ResumenMes;
import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.Periodo;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Lee v_resumen_mensual y las tablas de origen. Solo lectura. */
@Component
@RequiredArgsConstructor
class DatosFinancierosJdbcAdapter implements DatosFinancierosPort {

    private final JdbcClient jdbc;

    @Override
    public ResumenMes mes(UUID hogarId, Periodo periodo) {
        var cifras = jdbc.sql("""
                        select ingresos, gastos, pagos_deuda, aportes_meta, saldo_disponible, apartado_total, apartado_pendiente
                        from v_resumen_mensual where hogar_id = ? and periodo = ?""")
                .params(hogarId, periodo.inicio())
                .query((rs, n) -> new Cifras(rs.getBigDecimal(1), rs.getBigDecimal(2), rs.getBigDecimal(3),
                        rs.getBigDecimal(4), rs.getBigDecimal(5), rs.getBigDecimal(6), rs.getBigDecimal(7)))
                .optional()
                .orElse(new Cifras(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO));

        // El diezmo se mide por el ingreso que lo originó (periodo del apartado), no por la fecha de entrega
        BigDecimal diezmoApartado = suma("""
                select coalesce(sum(monto), 0) from apartado_mayordomia
                where hogar_id = ? and periodo = ? and tipo = 'DIEZMO' and estado <> 'ANULADO'""", hogarId, periodo);
        BigDecimal diezmoEntregado = suma("""
                select coalesce(sum(monto), 0) from apartado_mayordomia
                where hogar_id = ? and periodo = ? and tipo = 'DIEZMO' and estado = 'ENTREGADO'""", hogarId, periodo);
        BigDecimal cuotas = jdbc.sql("select coalesce(sum(cuota_mensual), 0) from deuda where hogar_id = ? and estado = 'ACTIVA'")
                .param(hogarId).query(BigDecimal.class).single();

        var indicadores = new IndicadoresTesoro(Dinero.de(cifras.ingresos()), Dinero.de(cifras.gastos()),
                Dinero.de(cifras.pagosDeuda()), Dinero.de(cifras.aportesMeta()), Dinero.de(diezmoApartado),
                Dinero.de(diezmoEntregado), Dinero.de(cuotas));
        return new ResumenMes(indicadores, Dinero.de(cifras.saldo()), Dinero.de(cifras.apartadoTotal()),
                Dinero.de(cifras.apartadoPendiente()));
    }

    @Override
    public List<LineaPresupuesto> presupuestoVsReal(UUID hogarId, Periodo periodo) {
        return jdbc.sql("""
                        select id, nombre, color, planeado, real from (
                            select c.id, c.nombre, c.color, c.orden, c.activa,
                                   coalesce(p.monto_planeado, c.presupuesto_mensual) as planeado,
                                   coalesce((select sum(m.monto) from movimiento m
                                             where m.categoria_id = c.id and m.tipo = 'GASTO' and m.deleted_at is null
                                               and m.fecha between ? and ?), 0) as real
                            from categoria c
                            left join presupuesto_mensual_periodo p on p.categoria_id = c.id and p.periodo = ?
                            where c.hogar_id = ? and c.tipo = 'GASTO'
                        ) t
                        where activa or real > 0
                        order by orden, nombre""")
                .params(periodo.inicio(), periodo.fin(), periodo.inicio(), hogarId)
                .query((rs, n) -> new LineaPresupuesto(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3),
                        Dinero.de(rs.getBigDecimal(4)), Dinero.de(rs.getBigDecimal(5))))
                .list();
    }

    private BigDecimal suma(String sql, UUID hogarId, Periodo periodo) {
        return jdbc.sql(sql).params(hogarId, periodo.inicio()).query(BigDecimal.class).single();
    }

    private record Cifras(BigDecimal ingresos, BigDecimal gastos, BigDecimal pagosDeuda, BigDecimal aportesMeta,
            BigDecimal saldo, BigDecimal apartadoTotal, BigDecimal apartadoPendiente) {
    }
}
