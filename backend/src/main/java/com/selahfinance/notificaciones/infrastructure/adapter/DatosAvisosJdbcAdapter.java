package com.selahfinance.notificaciones.infrastructure.adapter;

import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.CuentaConHogar;
import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.CuotaPorVencer;
import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.DatosAvisosPort;
import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.DiezmoPendiente;
import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.EstadoPresupuesto;
import com.selahfinance.notificaciones.application.port.out.NotificacionesPorts.ResumenSemanal;
import com.selahfinance.shared.domain.Dinero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Consultas de solo lectura sobre datos de otros módulos para armar los avisos. */
@Component
@RequiredArgsConstructor
class DatosAvisosJdbcAdapter implements DatosAvisosPort {

    private final JdbcClient jdbc;

    @Override
    public List<DiezmoPendiente> diezmosPendientes(LocalDate primerDiaDelMes, int diaDelMes) {
        return jdbc.sql("""
                        select mh.usuario_id, a.hogar_id, sum(a.monto)
                        from apartado_mayordomia a
                        join configuracion_mayordomia c on c.hogar_id = a.hogar_id
                        join miembro_hogar mh on mh.hogar_id = a.hogar_id and mh.usuario_id is not null and mh.activo
                        where a.tipo = 'DIEZMO' and a.estado = 'PENDIENTE' and a.periodo = ?
                          and c.dia_entrega_diezmo is not null and c.dia_entrega_diezmo <= ?
                        group by mh.usuario_id, a.hogar_id""")
                .params(primerDiaDelMes, diaDelMes)
                .query((rs, n) -> new DiezmoPendiente(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class),
                        Dinero.de(rs.getBigDecimal(3))))
                .list();
    }

    @Override
    public List<CuotaPorVencer> cuotasQueVencenElDia(int diaDelMes) {
        return jdbc.sql("""
                        select mh.usuario_id, d.hogar_id, d.id, d.nombre, d.cuota_mensual
                        from deuda d
                        join miembro_hogar mh on mh.hogar_id = d.hogar_id and mh.usuario_id is not null and mh.activo
                        where d.estado = 'ACTIVA' and d.dia_pago = ?""")
                .param(diaDelMes)
                .query((rs, n) -> new CuotaPorVencer(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class),
                        rs.getObject(3, UUID.class), rs.getString(4), Dinero.de(rs.getBigDecimal(5))))
                .list();
    }

    @Override
    public List<CuentaConHogar> cuentasConHogar() {
        return jdbc.sql("""
                        select u.id, min(mh.hogar_id::text)::uuid
                        from usuario u
                        join miembro_hogar mh on mh.usuario_id = u.id and mh.activo
                        where u.estado = 'ACTIVO' and u.rol in ('HERMANO', 'PASTOR')
                        group by u.id""")
                .query((rs, n) -> new CuentaConHogar(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class)))
                .list();
    }

    @Override
    public ResumenSemanal resumenSemanal(UUID hogarId, LocalDate desde, LocalDate hasta) {
        var cifras = jdbc.sql("""
                        select coalesce(sum(monto) filter (where tipo = 'INGRESO'), 0),
                               coalesce(sum(monto) filter (where tipo = 'GASTO'), 0)
                        from movimiento
                        where hogar_id = ? and deleted_at is null and fecha between ? and ?""")
                .params(hogarId, desde, hasta)
                .query((rs, n) -> new BigDecimal[] { rs.getBigDecimal(1), rs.getBigDecimal(2) })
                .single();
        return new ResumenSemanal(Dinero.de(cifras[0]), Dinero.de(cifras[1]));
    }

    @Override
    public Optional<EstadoPresupuesto> estadoPresupuesto(UUID hogarId, UUID categoriaId, LocalDate fecha) {
        var mes = YearMonth.from(fecha);
        return jdbc.sql("""
                        select c.nombre,
                               coalesce(p.monto_planeado, c.presupuesto_mensual),
                               coalesce((select sum(m.monto) from movimiento m
                                         where m.categoria_id = c.id and m.tipo = 'GASTO' and m.deleted_at is null
                                           and m.fecha between ? and ?), 0)
                        from categoria c
                        left join presupuesto_mensual_periodo p on p.categoria_id = c.id and p.periodo = ?
                        where c.id = ? and c.hogar_id = ?""")
                .params(mes.atDay(1), mes.atEndOfMonth(), mes.atDay(1), categoriaId, hogarId)
                .query((rs, n) -> new EstadoPresupuesto(rs.getString(1), Dinero.de(rs.getBigDecimal(2)),
                        Dinero.de(rs.getBigDecimal(3))))
                .optional();
    }
}
