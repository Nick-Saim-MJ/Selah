package com.selahfinance.reflexion.infrastructure.adapter;

import com.selahfinance.mayordomia.application.port.in.ConsultarResumenMayordomiaUseCase;
import com.selahfinance.reflexion.application.port.out.ReflexionPorts.PreguntaRepositoryPort;
import com.selahfinance.reflexion.application.port.out.ReflexionPorts.RespuestaRepositoryPort;
import com.selahfinance.reflexion.application.port.out.ReflexionPorts.ResumenSemanaPort;
import com.selahfinance.reflexion.domain.model.PreguntaReflexion;
import com.selahfinance.reflexion.domain.model.ResumenSemana;
import com.selahfinance.shared.domain.DimensionMayordomia;
import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.Periodo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Preguntas, respuestas y resumen de la semana (lectura de movimientos + estado del diezmo). */
@Component
@RequiredArgsConstructor
class ReflexionJdbcAdapter implements PreguntaRepositoryPort, RespuestaRepositoryPort, ResumenSemanaPort {

    private final JdbcClient jdbc;
    private final ConsultarResumenMayordomiaUseCase mayordomia;

    @Override
    public List<PreguntaReflexion> activas() {
        return jdbc.sql("select id, texto, referencia_biblica, dimension from pregunta_reflexion where activa order by id")
                .query((rs, n) -> new PreguntaReflexion(rs.getShort(1), rs.getString(2), rs.getString(3),
                        rs.getString(4) == null ? null : DimensionMayordomia.valueOf(rs.getString(4))))
                .list();
    }

    @Override
    public Optional<String> respuesta(UUID usuarioId, LocalDate semanaInicio) {
        return jdbc.sql("select respuesta from reflexion_semanal where usuario_id = ? and semana_inicio = ?")
                .params(usuarioId, semanaInicio)
                .query((rs, n) -> Optional.ofNullable(rs.getString(1)))
                .optional()
                .flatMap(o -> o);
    }

    @Override
    public void guardar(UUID usuarioId, LocalDate semanaInicio, short preguntaId, String respuesta) {
        jdbc.sql("""
                        insert into reflexion_semanal (usuario_id, semana_inicio, pregunta_id, respuesta)
                        values (?, ?, ?, ?)
                        on conflict (usuario_id, semana_inicio) do update set respuesta = excluded.respuesta""")
                .params(usuarioId, semanaInicio, preguntaId, respuesta)
                .update();
    }

    @Override
    public ResumenSemana resumen(UUID hogarId, LocalDate desde, LocalDate hasta) {
        var cifras = jdbc.sql("""
                        select coalesce(sum(monto) filter (where tipo = 'INGRESO'), 0),
                               coalesce(sum(monto) filter (where tipo = 'GASTO'), 0)
                        from movimiento
                        where hogar_id = ? and deleted_at is null and fecha between ? and ?""")
                .params(hogarId, desde, hasta)
                .query((rs, n) -> new BigDecimal[] { rs.getBigDecimal(1), rs.getBigDecimal(2) })
                .single();
        var diezmo = mayordomia.resumen(hogarId, Periodo.de(hasta));
        Boolean alDia = diezmo.diezmoApartado().esPositivo() ? diezmo.diezmoAlDia() : null;
        return new ResumenSemana(Dinero.de(cifras[0]), Dinero.de(cifras[1]), alDia);
    }
}
