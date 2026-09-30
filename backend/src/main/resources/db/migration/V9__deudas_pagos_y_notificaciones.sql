-- =====================================================================
-- SelahFinance · V9 · Pagos de deuda (capital vs. interés) y dedupe de notificaciones
-- =====================================================================

-- Cada PAGO_DEUDA se descompone en capital e interés. Vive en el módulo deudas
-- (movimiento no conoce deudas), y permite revertir el saldo si se elimina el pago.
CREATE TABLE pago_deuda (
    movimiento_id  UUID PRIMARY KEY REFERENCES movimiento (id),
    deuda_id       UUID           NOT NULL REFERENCES deuda (id),
    monto_interes  NUMERIC(14, 2) NOT NULL CHECK (monto_interes >= 0),
    monto_capital  NUMERIC(14, 2) NOT NULL CHECK (monto_capital >= 0),
    revertido_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE INDEX ix_pago_deuda_deuda ON pago_deuda (deuda_id);

-- Columnas de V3 que nunca se usaron (ahora están en pago_deuda)
ALTER TABLE movimiento DROP COLUMN monto_interes;
ALTER TABLE movimiento DROP COLUMN monto_capital;

-- Evita duplicar recordatorios (p. ej. "diezmo pendiente 2026-09" una sola vez por usuario)
ALTER TABLE notificacion ADD COLUMN clave_dedupe VARCHAR(120);
CREATE UNIQUE INDEX ux_notificacion_dedupe ON notificacion (usuario_id, clave_dedupe) WHERE clave_dedupe IS NOT NULL;
