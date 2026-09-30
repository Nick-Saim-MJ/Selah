-- =====================================================================
-- SelahFinance · V4 · Integraciones con otros sistemas (APIs)
-- Otros equipos desarrollan módulos independientes; este esquema cubre
-- las tres direcciones de integración:
--   A) Ellos CONSUMEN nuestra API          -> cliente_api (API keys)
--   B) Nosotros CONSUMIMOS sus APIs        -> proveedor_externo
--   C) Les AVISAMOS de lo que ocurre aquí  -> evento_outbox + suscripcion_webhook
-- =====================================================================

-- A) Clientes de nuestra API ------------------------------------------
CREATE TABLE cliente_api (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre             VARCHAR(80)  NOT NULL UNIQUE,
    descripcion        VARCHAR(250),
    responsable_email  CITEXT,
    api_key_prefijo    VARCHAR(12)  NOT NULL UNIQUE,   -- parte visible para identificar la key
    api_key_hash       VARCHAR(128) NOT NULL,          -- SHA-256; la key completa se muestra una sola vez
    scopes             TEXT[]       NOT NULL DEFAULT '{}',  -- p. ej. {movimientos:read, habitos:write}
    activo             BOOLEAN      NOT NULL DEFAULT TRUE,
    expira_at          TIMESTAMPTZ,
    ultimo_uso_at      TIMESTAMPTZ,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- B) APIs externas que consumimos -------------------------------------
-- Los secretos NO se guardan aquí: se leen de variables de entorno
-- (SELAH_PROVEEDOR_<CODIGO>_SECRET) para no exponerlos en la BD.
CREATE TABLE proveedor_externo (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo       VARCHAR(40)  NOT NULL UNIQUE,
    nombre       VARCHAR(80)  NOT NULL,
    descripcion  VARCHAR(250),
    dimension    VARCHAR(10)  CHECK (dimension IN ('TIEMPO', 'TALENTO', 'TESORO', 'TEMPLO')),
    base_url     VARCHAR(255) NOT NULL,
    tipo_auth    VARCHAR(12)  NOT NULL DEFAULT 'API_KEY'
                 CHECK (tipo_auth IN ('NINGUNA', 'API_KEY', 'BEARER', 'OAUTH2_CC')),
    timeout_ms   INTEGER      NOT NULL DEFAULT 5000 CHECK (timeout_ms BETWEEN 100 AND 60000),
    activo       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_proveedor_updated BEFORE UPDATE ON proveedor_externo
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Identidad del usuario en el sistema externo
CREATE TABLE vinculo_usuario_externo (
    usuario_id    UUID         NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    proveedor_id  UUID         NOT NULL REFERENCES proveedor_externo (id) ON DELETE CASCADE,
    id_externo    VARCHAR(100) NOT NULL,
    vinculado_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (usuario_id, proveedor_id),
    UNIQUE (proveedor_id, id_externo)
);

-- C) Eventos de dominio (patrón Outbox) y webhooks ---------------------
-- Se escriben en la MISMA transacción que el cambio de negocio; un job
-- los publica después. Así nunca se pierde un evento ni se envía uno
-- de una transacción que hizo rollback.
CREATE TABLE evento_outbox (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_evento   VARCHAR(80)  NOT NULL,     -- p. ej. movimiento.registrado
    agregado      VARCHAR(40)  NOT NULL,     -- p. ej. Movimiento
    agregado_id   UUID         NOT NULL,
    hogar_id      UUID,
    payload       JSONB        NOT NULL,
    estado        VARCHAR(10)  NOT NULL DEFAULT 'PENDIENTE'
                  CHECK (estado IN ('PENDIENTE', 'PUBLICADO', 'FALLIDO')),
    intentos      SMALLINT     NOT NULL DEFAULT 0,
    ultimo_error  TEXT,
    ocurrido_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    publicado_at  TIMESTAMPTZ
);
CREATE INDEX ix_outbox_pendientes ON evento_outbox (ocurrido_at) WHERE estado = 'PENDIENTE';

CREATE TABLE suscripcion_webhook (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_api_id   UUID         NOT NULL REFERENCES cliente_api (id) ON DELETE CASCADE,
    tipo_evento      VARCHAR(80)  NOT NULL,
    url_destino      VARCHAR(255) NOT NULL,
    secreto_cifrado  VARCHAR(255) NOT NULL,  -- para firmar (HMAC-SHA256) el cuerpo; cifrado con clave de entorno
    activa           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (cliente_api_id, tipo_evento, url_destino)
);

-- Auditoría (app financiera: quién cambió qué y cuándo) ----------------
CREATE TABLE auditoria (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      UUID REFERENCES usuario (id) ON DELETE SET NULL,
    cliente_api_id  UUID REFERENCES cliente_api (id) ON DELETE SET NULL,
    entidad         VARCHAR(40) NOT NULL,
    entidad_id      VARCHAR(40) NOT NULL,
    accion          VARCHAR(12) NOT NULL
                    CHECK (accion IN ('CREAR', 'ACTUALIZAR', 'ELIMINAR', 'ENTREGAR', 'LOGIN')),
    datos_antes     JSONB,
    datos_despues   JSONB,
    ip              INET,
    ocurrido_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_auditoria_entidad ON auditoria (entidad, entidad_id);
