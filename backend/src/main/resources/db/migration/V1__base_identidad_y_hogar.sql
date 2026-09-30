-- =====================================================================
-- SelahFinance · V1 · Base, identidad y hogar
-- PostgreSQL 14+  (gen_random_uuid() es nativo desde PG13)
-- Convenciones:
--   * PK UUID, timestamps TIMESTAMPTZ, dinero NUMERIC(14,2) (nunca FLOAT).
--   * Enumeraciones como VARCHAR + CHECK (más simple de mapear con JPA
--     y de evolucionar con migraciones que un tipo ENUM nativo).
--   * Las reglas de negocio viven en el dominio (backend); la BD solo
--     garantiza integridad (FK, CHECK, UNIQUE).
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS citext;   -- emails sin distinguir mayúsculas

CREATE OR REPLACE FUNCTION fn_set_updated_at() RETURNS trigger AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------
-- Usuarios y sesiones
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email                  CITEXT       NOT NULL UNIQUE,
    password_hash          VARCHAR(100) NOT NULL,
    nombres                VARCHAR(80)  NOT NULL,
    apellidos              VARCHAR(80),
    telefono               VARCHAR(20),
    iglesia_local          VARCHAR(120),
    rol                    VARCHAR(20)  NOT NULL DEFAULT 'USUARIO'
                           CHECK (rol IN ('USUARIO', 'ADMIN')),
    estado                 VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'
                           CHECK (estado IN ('ACTIVO', 'BLOQUEADO', 'ELIMINADO')),
    onboarding_completado  BOOLEAN      NOT NULL DEFAULT FALSE,
    ultimo_acceso_at       TIMESTAMPTZ,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_usuario_updated BEFORE UPDATE ON usuario
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE refresh_token (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id   UUID         NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    token_hash   VARCHAR(128) NOT NULL UNIQUE,      -- SHA-256 del token, nunca el token plano
    user_agent   VARCHAR(200),
    expira_at    TIMESTAMPTZ  NOT NULL,
    revocado_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_refresh_token_usuario ON refresh_token (usuario_id);

-- ---------------------------------------------------------------------
-- Hogar (individual o familiar) y sus miembros
-- Toda la información financiera cuelga del hogar, no del usuario,
-- para soportar finanzas familiares con varios miembros.
-- ---------------------------------------------------------------------
CREATE TABLE hogar (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre        VARCHAR(100) NOT NULL,
    tipo          VARCHAR(15)  NOT NULL DEFAULT 'INDIVIDUAL'
                  CHECK (tipo IN ('INDIVIDUAL', 'FAMILIAR')),
    moneda        CHAR(3)      NOT NULL DEFAULT 'PEN' CHECK (moneda ~ '^[A-Z]{3}$'),  -- ISO 4217
    zona_horaria  VARCHAR(50)  NOT NULL DEFAULT 'America/Lima',
    -- Coordenadas opcionales para calcular la puesta de sol (Modo Sábado)
    latitud       NUMERIC(9, 6) CHECK (latitud BETWEEN -90 AND 90),
    longitud      NUMERIC(9, 6) CHECK (longitud BETWEEN -180 AND 180),
    creado_por    UUID         NOT NULL REFERENCES usuario (id),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_hogar_updated BEFORE UPDATE ON hogar
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE miembro_hogar (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hogar_id    UUID        NOT NULL REFERENCES hogar (id) ON DELETE CASCADE,
    usuario_id  UUID        REFERENCES usuario (id) ON DELETE SET NULL,  -- NULL: miembro sin cuenta (p. ej. hijos)
    nombre      VARCHAR(80) NOT NULL,
    parentesco  VARCHAR(30),
    rol         VARCHAR(15) NOT NULL DEFAULT 'MIEMBRO'
                CHECK (rol IN ('ADMINISTRADOR', 'MIEMBRO', 'LECTOR')),
    activo      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (hogar_id, usuario_id)
);
CREATE INDEX ix_miembro_usuario ON miembro_hogar (usuario_id);

-- ---------------------------------------------------------------------
-- Configuración de mayordomía (wizard paso 3) — 1:1 con hogar
-- ---------------------------------------------------------------------
CREATE TABLE configuracion_mayordomia (
    hogar_id            UUID PRIMARY KEY REFERENCES hogar (id) ON DELETE CASCADE,
    pct_diezmo          NUMERIC(5, 2) NOT NULL DEFAULT 10.00 CHECK (pct_diezmo BETWEEN 0 AND 100),
    ofrenda_activa      BOOLEAN       NOT NULL DEFAULT TRUE,
    pct_ofrenda         NUMERIC(5, 2) NOT NULL DEFAULT 2.00 CHECK (pct_ofrenda BETWEEN 0 AND 100),
    dia_entrega_diezmo  SMALLINT      CHECK (dia_entrega_diezmo BETWEEN 1 AND 31),
    modo_sabado_activo  BOOLEAN       NOT NULL DEFAULT TRUE,
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CHECK (pct_diezmo + pct_ofrenda <= 100)
);
CREATE TRIGGER trg_config_mayordomia_updated BEFORE UPDATE ON configuracion_mayordomia
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE preferencia_notificacion (
    usuario_id           UUID PRIMARY KEY REFERENCES usuario (id) ON DELETE CASCADE,
    recordatorio_diezmo  BOOLEAN     NOT NULL DEFAULT TRUE,
    recordatorio_deudas  BOOLEAN     NOT NULL DEFAULT TRUE,
    resumen_semanal      BOOLEAN     NOT NULL DEFAULT TRUE,
    reflexion_sabado     BOOLEAN     NOT NULL DEFAULT TRUE,
    hora_resumen         TIME        NOT NULL DEFAULT '19:00',
    push_token           VARCHAR(255),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_pref_notif_updated BEFORE UPDATE ON preferencia_notificacion
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
