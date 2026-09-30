-- =====================================================================
-- SelahFinance · V3 · Movimientos (fuente única de verdad) y mayordomía
-- =====================================================================

-- ---------------------------------------------------------------------
-- Movimiento: TODO flujo de dinero se registra aquí (Lucas 16:10).
-- Las demás pantallas son vistas derivadas.
-- ---------------------------------------------------------------------
CREATE TABLE movimiento (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hogar_id            UUID           NOT NULL REFERENCES hogar (id) ON DELETE CASCADE,
    registrado_por      UUID           NOT NULL REFERENCES usuario (id),
    tipo                VARCHAR(12)    NOT NULL
                        CHECK (tipo IN ('INGRESO', 'GASTO', 'DIEZMO', 'OFRENDA', 'PAGO_DEUDA', 'APORTE_META')),
    monto               NUMERIC(14, 2) NOT NULL CHECK (monto > 0),
    fecha               DATE           NOT NULL,
    descripcion         VARCHAR(160),
    nota                TEXT,
    categoria_id        UUID REFERENCES categoria (id),
    fuente_ingreso_id   UUID REFERENCES fuente_ingreso (id),
    meta_id             UUID REFERENCES meta_ahorro (id),
    deuda_id            UUID REFERENCES deuda (id),
    destino_ofrenda_id  UUID REFERENCES destino_ofrenda (id),
    es_presupuestado    BOOLEAN        NOT NULL DEFAULT TRUE,   -- FALSE = gasto imprevisto
    -- Descomposición de un PAGO_DEUDA (la calcula el dominio)
    monto_interes       NUMERIC(14, 2) CHECK (monto_interes >= 0),
    monto_capital       NUMERIC(14, 2) CHECK (monto_capital >= 0),
    -- Trazabilidad de integraciones: permite idempotencia si otro sistema
    -- (módulo de un compañero) crea movimientos vía API.
    origen              VARCHAR(12)    NOT NULL DEFAULT 'APP'
                        CHECK (origen IN ('APP', 'API_EXTERNA', 'IMPORTACION')),
    referencia_externa  VARCHAR(100),
    version             INTEGER        NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    deleted_at          TIMESTAMPTZ,   -- borrado lógico: un registro financiero nunca se borra físicamente

    CONSTRAINT ck_mov_gasto_categoria   CHECK (tipo <> 'GASTO'       OR categoria_id IS NOT NULL),
    CONSTRAINT ck_mov_pago_deuda        CHECK (tipo <> 'PAGO_DEUDA'  OR deuda_id     IS NOT NULL),
    CONSTRAINT ck_mov_aporte_meta       CHECK (tipo <> 'APORTE_META' OR meta_id      IS NOT NULL),
    CONSTRAINT ck_mov_destino_ofrenda   CHECK (destino_ofrenda_id IS NULL OR tipo = 'OFRENDA'),
    CONSTRAINT ux_mov_referencia_externa UNIQUE (hogar_id, origen, referencia_externa)
);
CREATE TRIGGER trg_movimiento_updated BEFORE UPDATE ON movimiento
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE INDEX ix_mov_hogar_fecha ON movimiento (hogar_id, fecha DESC) WHERE deleted_at IS NULL;
CREATE INDEX ix_mov_hogar_tipo  ON movimiento (hogar_id, tipo, fecha) WHERE deleted_at IS NULL;
CREATE INDEX ix_mov_categoria   ON movimiento (categoria_id) WHERE categoria_id IS NOT NULL;
CREATE INDEX ix_mov_meta        ON movimiento (meta_id)      WHERE meta_id IS NOT NULL;
CREATE INDEX ix_mov_deuda       ON movimiento (deuda_id)     WHERE deuda_id IS NOT NULL;

-- ---------------------------------------------------------------------
-- Apartado de mayordomía (primicias · Prov. 3:9, Mal. 3:10)
-- Al registrar un INGRESO, el dominio genera 1 apartado de DIEZMO y,
-- si aplica, 1 de OFRENDA. Quedan PENDIENTE hasta que el usuario marca
-- "entregado", lo que crea un movimiento DIEZMO/OFRENDA y lo enlaza.
--
-- Saldo disponible del mes =
--     ingresos − apartados PENDIENTES − movimientos DIEZMO/OFRENDA
--     − gastos − pagos de deuda − aportes a metas
-- (el diezmo se descuenta desde que se aparta, no cuando se entrega)
-- ---------------------------------------------------------------------
CREATE TABLE apartado_mayordomia (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hogar_id               UUID           NOT NULL REFERENCES hogar (id) ON DELETE CASCADE,
    movimiento_ingreso_id  UUID           NOT NULL REFERENCES movimiento (id),
    tipo                   VARCHAR(10)    NOT NULL CHECK (tipo IN ('DIEZMO', 'OFRENDA')),
    porcentaje             NUMERIC(5, 2)  NOT NULL CHECK (porcentaje BETWEEN 0 AND 100),
    base_calculo           NUMERIC(14, 2) NOT NULL CHECK (base_calculo >= 0),
    monto                  NUMERIC(14, 2) NOT NULL CHECK (monto >= 0),
    periodo                DATE           NOT NULL CHECK (EXTRACT(DAY FROM periodo) = 1),
    estado                 VARCHAR(10)    NOT NULL DEFAULT 'PENDIENTE'
                           CHECK (estado IN ('PENDIENTE', 'ENTREGADO', 'ANULADO')),
    movimiento_entrega_id  UUID REFERENCES movimiento (id),
    entregado_at           TIMESTAMPTZ,
    created_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
    UNIQUE (movimiento_ingreso_id, tipo),
    CONSTRAINT ck_apartado_entregado CHECK (estado <> 'ENTREGADO' OR movimiento_entrega_id IS NOT NULL)
);
CREATE INDEX ix_apartado_hogar_periodo ON apartado_mayordomia (hogar_id, periodo, estado);
