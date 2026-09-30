-- =====================================================================
-- SelahFinance · V5 · Mayordomía integral: Tiempo, Talento, Tesoro, Templo
--
--   TESORO  -> se calcula desde las finanzas (movimientos, apartados,
--              metas, deudas). No requiere registro manual.
--   TIEMPO, TALENTO, TEMPLO -> hábitos registrados en la app o
--              provistos por los módulos de otros equipos (proveedor_externo).
-- =====================================================================

-- Catálogo de hábitos/indicadores. hogar_id NULL = catálogo del sistema.
CREATE TABLE habito_mayordomia (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hogar_id            UUID          REFERENCES hogar (id) ON DELETE CASCADE,
    codigo              VARCHAR(40)   NOT NULL,
    dimension           VARCHAR(10)   NOT NULL CHECK (dimension IN ('TIEMPO', 'TALENTO', 'TESORO', 'TEMPLO')),
    nombre              VARCHAR(80)   NOT NULL,
    descripcion         VARCHAR(250),
    unidad              VARCHAR(10)   NOT NULL
                        CHECK (unidad IN ('MINUTOS', 'HORAS', 'VECES', 'BOOLEANO', 'VASOS', 'PASOS')),
    frecuencia_meta     VARCHAR(8)    NOT NULL DEFAULT 'DIARIA' CHECK (frecuencia_meta IN ('DIARIA', 'SEMANAL')),
    meta_valor          NUMERIC(8, 2) NOT NULL CHECK (meta_valor > 0),
    referencia_biblica  VARCHAR(60),
    activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    orden               SMALLINT      NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX ux_habito_sistema ON habito_mayordomia (codigo) WHERE hogar_id IS NULL;
CREATE UNIQUE INDEX ux_habito_hogar   ON habito_mayordomia (hogar_id, codigo) WHERE hogar_id IS NOT NULL;

CREATE TABLE registro_habito (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id          UUID           NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    habito_id           UUID           NOT NULL REFERENCES habito_mayordomia (id),
    fecha               DATE           NOT NULL,
    valor               NUMERIC(10, 2) NOT NULL CHECK (valor >= 0),   -- BOOLEANO: 1 = cumplido
    nota                VARCHAR(250),
    fuente              VARCHAR(12)    NOT NULL DEFAULT 'APP' CHECK (fuente IN ('APP', 'API_EXTERNA')),
    proveedor_id        UUID           REFERENCES proveedor_externo (id),
    referencia_externa  VARCHAR(100),
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    UNIQUE (usuario_id, habito_id, fecha, fuente),
    CHECK (fuente = 'APP' OR proveedor_id IS NOT NULL)
);
CREATE INDEX ix_registro_habito_usuario_fecha ON registro_habito (usuario_id, fecha);

-- Reporte de mayordomía (snapshot). Guardarlo permite ver la evolución
-- histórica aunque cambien las reglas de cálculo (version_algoritmo).
-- Puntajes 0-100; NULL = dimensión sin datos en el periodo.
CREATE TABLE reporte_mayordomia (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hogar_id           UUID          NOT NULL REFERENCES hogar (id) ON DELETE CASCADE,
    usuario_id         UUID          REFERENCES usuario (id) ON DELETE CASCADE,   -- NULL = reporte del hogar
    tipo_periodo       VARCHAR(8)    NOT NULL CHECK (tipo_periodo IN ('SEMANAL', 'MENSUAL', 'ANUAL')),
    periodo_inicio     DATE          NOT NULL,
    periodo_fin        DATE          NOT NULL,
    puntaje_tiempo     NUMERIC(5, 2) CHECK (puntaje_tiempo  BETWEEN 0 AND 100),
    puntaje_talento    NUMERIC(5, 2) CHECK (puntaje_talento BETWEEN 0 AND 100),
    puntaje_tesoro     NUMERIC(5, 2) CHECK (puntaje_tesoro  BETWEEN 0 AND 100),
    puntaje_templo     NUMERIC(5, 2) CHECK (puntaje_templo  BETWEEN 0 AND 100),
    puntaje_global     NUMERIC(5, 2) CHECK (puntaje_global  BETWEEN 0 AND 100),
    semaforo           VARCHAR(8)    CHECK (semaforo IN ('VERDE', 'AMARILLO', 'ROJO')),
    detalle            JSONB         NOT NULL DEFAULT '{}'::jsonb,   -- métricas y fuentes consultadas
    version_algoritmo  VARCHAR(10)   NOT NULL DEFAULT '1.0',
    generado_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CHECK (periodo_fin >= periodo_inicio)
);
-- PG14 no soporta UNIQUE NULLS NOT DISTINCT -> índice con COALESCE
CREATE UNIQUE INDEX ux_reporte_periodo ON reporte_mayordomia
    (hogar_id, COALESCE(usuario_id, '00000000-0000-0000-0000-000000000000'::uuid), tipo_periodo, periodo_inicio);

-- ---------------------------------------------------------------------
-- Reflexión semanal (Lev. 23:3) — banco rotativo de preguntas
-- ---------------------------------------------------------------------
CREATE TABLE pregunta_reflexion (
    id                  SMALLSERIAL PRIMARY KEY,
    texto               VARCHAR(250) NOT NULL UNIQUE,
    referencia_biblica  VARCHAR(60),
    dimension           VARCHAR(10)  CHECK (dimension IN ('TIEMPO', 'TALENTO', 'TESORO', 'TEMPLO')),
    activa              BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE reflexion_semanal (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id     UUID        NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    semana_inicio  DATE        NOT NULL,             -- domingo de la semana
    pregunta_id    SMALLINT    NOT NULL REFERENCES pregunta_reflexion (id),
    respuesta      TEXT,                              -- opcional
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (usuario_id, semana_inicio)
);

-- ---------------------------------------------------------------------
-- Notificaciones. categoria CONSUMO se silencia en Modo Sábado.
-- ---------------------------------------------------------------------
CREATE TABLE notificacion (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id       UUID         NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    tipo             VARCHAR(40)  NOT NULL,     -- DIEZMO_PENDIENTE, CUOTA_DEUDA, RESUMEN_SEMANAL...
    categoria        VARCHAR(12)  NOT NULL
                     CHECK (categoria IN ('CONSUMO', 'MAYORDOMIA', 'RECORDATORIO', 'SISTEMA')),
    titulo           VARCHAR(120) NOT NULL,
    cuerpo           VARCHAR(500),
    datos            JSONB,                     -- deep link / payload para la app
    programada_para  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    enviada_at       TIMESTAMPTZ,
    leida_at         TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_notificacion_usuario ON notificacion (usuario_id, created_at DESC);
CREATE INDEX ix_notificacion_por_enviar ON notificacion (programada_para) WHERE enviada_at IS NULL;
