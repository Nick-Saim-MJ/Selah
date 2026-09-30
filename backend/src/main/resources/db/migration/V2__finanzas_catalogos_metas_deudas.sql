-- =====================================================================
-- SelahFinance · V2 · Catálogos financieros, presupuesto, metas y deudas
-- (se crean antes que `movimiento` porque éste las referencia)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Fuentes de ingreso (wizard paso 2)
-- ---------------------------------------------------------------------
CREATE TABLE fuente_ingreso (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hogar_id                UUID           NOT NULL REFERENCES hogar (id) ON DELETE CASCADE,
    nombre                  VARCHAR(60)    NOT NULL,
    monto_estimado_mensual  NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (monto_estimado_mensual >= 0),
    activa                  BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ    NOT NULL DEFAULT now(),
    UNIQUE (hogar_id, nombre)
);
CREATE TRIGGER trg_fuente_ingreso_updated BEFORE UPDATE ON fuente_ingreso
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- ---------------------------------------------------------------------
-- Categorías editables por el usuario (wizard paso 4)
-- presupuesto_mensual = monto por defecto; presupuesto_mensual_periodo
-- permite sobrescribirlo para un mes concreto (Presupuestado vs. Real).
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hogar_id             UUID           NOT NULL REFERENCES hogar (id) ON DELETE CASCADE,
    nombre               VARCHAR(60)    NOT NULL,
    tipo                 VARCHAR(10)    NOT NULL DEFAULT 'GASTO' CHECK (tipo IN ('GASTO', 'INGRESO')),
    color                VARCHAR(7)     CHECK (color ~ '^#[0-9A-Fa-f]{6}$'),
    icono                VARCHAR(40),
    presupuesto_mensual  NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (presupuesto_mensual >= 0),
    orden                SMALLINT       NOT NULL DEFAULT 0,
    activa               BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    UNIQUE (hogar_id, tipo, nombre)
);
CREATE TRIGGER trg_categoria_updated BEFORE UPDATE ON categoria
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE presupuesto_mensual_periodo (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    categoria_id    UUID           NOT NULL REFERENCES categoria (id) ON DELETE CASCADE,
    periodo         DATE           NOT NULL CHECK (EXTRACT(DAY FROM periodo) = 1),  -- primer día del mes
    monto_planeado  NUMERIC(14, 2) NOT NULL CHECK (monto_planeado >= 0),
    UNIQUE (categoria_id, periodo)
);

-- ---------------------------------------------------------------------
-- Metas de ahorro  (Prov. 6:6-8 · 1 Tim. 6:6-8)
-- monto_actual NO se guarda: se deriva de los movimientos APORTE_META
-- (ver vista v_meta_progreso) para no tener dos fuentes de verdad.
-- ---------------------------------------------------------------------
CREATE TABLE meta_ahorro (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hogar_id        UUID           NOT NULL REFERENCES hogar (id) ON DELETE CASCADE,
    nombre          VARCHAR(80)    NOT NULL,
    proposito       VARCHAR(250)   NOT NULL,   -- cada meta tiene un propósito nombrado
    tipo            VARCHAR(15)    NOT NULL DEFAULT 'ESPECIFICA'
                    CHECK (tipo IN ('EMERGENCIA', 'ESPECIFICA', 'LIBRE')),
    monto_objetivo  NUMERIC(14, 2) NOT NULL CHECK (monto_objetivo > 0),
    fecha_objetivo  DATE,
    es_principal    BOOLEAN        NOT NULL DEFAULT FALSE,
    estado          VARCHAR(12)    NOT NULL DEFAULT 'ACTIVA'
                    CHECK (estado IN ('ACTIVA', 'COMPLETADA', 'PAUSADA', 'CANCELADA')),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_meta_updated BEFORE UPDATE ON meta_ahorro
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
-- Solo una meta principal activa por hogar (la que se muestra en Inicio)
CREATE UNIQUE INDEX ux_meta_principal ON meta_ahorro (hogar_id)
    WHERE es_principal AND estado = 'ACTIVA';

-- ---------------------------------------------------------------------
-- Deudas  (Rom. 13:8 · Prov. 22:7)
-- cuota_mensual la calcula el dominio (sistema francés) a partir de
-- saldo, tasa y plazo; nunca se tipea a mano.
-- ---------------------------------------------------------------------
CREATE TABLE deuda (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hogar_id        UUID           NOT NULL REFERENCES hogar (id) ON DELETE CASCADE,
    nombre          VARCHAR(80)    NOT NULL,
    acreedor        VARCHAR(80),
    monto_original  NUMERIC(14, 2) NOT NULL CHECK (monto_original > 0),
    saldo_actual    NUMERIC(14, 2) NOT NULL CHECK (saldo_actual >= 0),
    tasa_anual      NUMERIC(7, 4)  NOT NULL DEFAULT 0 CHECK (tasa_anual >= 0),  -- TEA en %
    plazo_meses     SMALLINT       NOT NULL CHECK (plazo_meses > 0),
    cuota_mensual   NUMERIC(14, 2) NOT NULL CHECK (cuota_mensual >= 0),
    fecha_inicio    DATE           NOT NULL,
    dia_pago        SMALLINT       CHECK (dia_pago BETWEEN 1 AND 31),
    estado          VARCHAR(12)    NOT NULL DEFAULT 'ACTIVA'
                    CHECK (estado IN ('ACTIVA', 'PAGADA', 'REFINANCIADA')),
    version         INTEGER        NOT NULL DEFAULT 0,  -- bloqueo optimista (saldo cambia con cada pago)
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CHECK (saldo_actual <= monto_original)
);
CREATE TRIGGER trg_deuda_updated BEFORE UPDATE ON deuda
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE INDEX ix_deuda_hogar ON deuda (hogar_id) WHERE estado = 'ACTIVA';

-- ---------------------------------------------------------------------
-- Destinos de ofrenda específica (misión, construcción de templo, etc.)
-- hogar_id NULL = catálogo global sembrado por el sistema.
-- ---------------------------------------------------------------------
CREATE TABLE destino_ofrenda (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hogar_id     UUID         REFERENCES hogar (id) ON DELETE CASCADE,
    nombre       VARCHAR(80)  NOT NULL,
    descripcion  VARCHAR(250),
    activo       BOOLEAN      NOT NULL DEFAULT TRUE
);
CREATE UNIQUE INDEX ux_destino_global ON destino_ofrenda (nombre) WHERE hogar_id IS NULL;
CREATE UNIQUE INDEX ux_destino_hogar  ON destino_ofrenda (hogar_id, nombre) WHERE hogar_id IS NOT NULL;
