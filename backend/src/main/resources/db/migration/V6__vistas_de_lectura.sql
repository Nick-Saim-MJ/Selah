-- =====================================================================
-- SelahFinance · V6 · Vistas de lectura (agregados para Inicio/Reportes)
-- Solo agregan datos; los umbrales (semáforo, 40% de deuda, etc.) son
-- reglas de negocio y se evalúan en el dominio del backend.
-- =====================================================================

-- Totales por hogar y mes a partir de la fuente única (movimiento)
CREATE VIEW v_resumen_mensual AS
WITH mov AS (
    SELECT hogar_id,
           date_trunc('month', fecha)::date                                 AS periodo,
           COALESCE(SUM(monto) FILTER (WHERE tipo = 'INGRESO'), 0)          AS ingresos,
           COALESCE(SUM(monto) FILTER (WHERE tipo = 'GASTO'), 0)            AS gastos,
           COALESCE(SUM(monto) FILTER (WHERE tipo = 'DIEZMO'), 0)           AS diezmos_entregados,
           COALESCE(SUM(monto) FILTER (WHERE tipo = 'OFRENDA'), 0)          AS ofrendas_entregadas,
           COALESCE(SUM(monto) FILTER (WHERE tipo = 'PAGO_DEUDA'), 0)       AS pagos_deuda,
           COALESCE(SUM(monto) FILTER (WHERE tipo = 'APORTE_META'), 0)      AS aportes_meta,
           COALESCE(SUM(monto) FILTER (WHERE tipo = 'GASTO' AND NOT es_presupuestado), 0) AS gastos_imprevistos
    FROM movimiento
    WHERE deleted_at IS NULL
    GROUP BY hogar_id, date_trunc('month', fecha)
),
apt AS (
    SELECT hogar_id,
           periodo,
           COALESCE(SUM(monto) FILTER (WHERE estado <> 'ANULADO'), 0)   AS apartado_total,
           COALESCE(SUM(monto) FILTER (WHERE estado = 'PENDIENTE'), 0)  AS apartado_pendiente
    FROM apartado_mayordomia
    GROUP BY hogar_id, periodo
)
SELECT COALESCE(mov.hogar_id, apt.hogar_id)      AS hogar_id,
       COALESCE(mov.periodo, apt.periodo)        AS periodo,
       COALESCE(mov.ingresos, 0)                 AS ingresos,
       COALESCE(mov.gastos, 0)                   AS gastos,
       COALESCE(mov.gastos_imprevistos, 0)       AS gastos_imprevistos,
       COALESCE(mov.diezmos_entregados, 0)       AS diezmos_entregados,
       COALESCE(mov.ofrendas_entregadas, 0)      AS ofrendas_entregadas,
       COALESCE(mov.pagos_deuda, 0)              AS pagos_deuda,
       COALESCE(mov.aportes_meta, 0)             AS aportes_meta,
       COALESCE(apt.apartado_total, 0)           AS apartado_total,
       COALESCE(apt.apartado_pendiente, 0)       AS apartado_pendiente,
       COALESCE(mov.ingresos, 0)
         - COALESCE(apt.apartado_pendiente, 0)
         - COALESCE(mov.diezmos_entregados, 0)
         - COALESCE(mov.ofrendas_entregadas, 0)
         - COALESCE(mov.gastos, 0)
         - COALESCE(mov.pagos_deuda, 0)
         - COALESCE(mov.aportes_meta, 0)         AS saldo_disponible
FROM mov
FULL OUTER JOIN apt ON apt.hogar_id = mov.hogar_id AND apt.periodo = mov.periodo;

-- Presupuestado vs. real por categoría de gasto y mes
CREATE VIEW v_presupuesto_vs_real AS
WITH periodos AS (
    SELECT DISTINCT categoria_id, date_trunc('month', fecha)::date AS periodo
    FROM movimiento
    WHERE tipo = 'GASTO' AND deleted_at IS NULL
    UNION
    SELECT categoria_id, periodo FROM presupuesto_mensual_periodo
),
reales AS (
    SELECT categoria_id, date_trunc('month', fecha)::date AS periodo, SUM(monto) AS monto_real
    FROM movimiento
    WHERE tipo = 'GASTO' AND deleted_at IS NULL
    GROUP BY categoria_id, date_trunc('month', fecha)
)
SELECT c.hogar_id,
       c.id                                                  AS categoria_id,
       c.nombre                                              AS categoria,
       c.color,
       p.periodo,
       COALESCE(pmp.monto_planeado, c.presupuesto_mensual)   AS monto_planeado,
       COALESCE(r.monto_real, 0)                             AS monto_real
FROM periodos p
JOIN categoria c ON c.id = p.categoria_id AND c.tipo = 'GASTO'
LEFT JOIN presupuesto_mensual_periodo pmp ON pmp.categoria_id = p.categoria_id AND pmp.periodo = p.periodo
LEFT JOIN reales r ON r.categoria_id = p.categoria_id AND r.periodo = p.periodo;

-- Progreso de metas de ahorro (monto_actual derivado de APORTE_META)
CREATE VIEW v_meta_progreso AS
SELECT m.id              AS meta_id,
       m.hogar_id,
       m.nombre,
       m.proposito,
       m.tipo,
       m.monto_objetivo,
       m.fecha_objetivo,
       m.es_principal,
       m.estado,
       COALESCE(SUM(mv.monto), 0) AS monto_actual,
       ROUND(LEAST(COALESCE(SUM(mv.monto), 0) / m.monto_objetivo * 100, 100), 2) AS porcentaje
FROM meta_ahorro m
LEFT JOIN movimiento mv ON mv.meta_id = m.id AND mv.tipo = 'APORTE_META' AND mv.deleted_at IS NULL
GROUP BY m.id;
