-- =====================================================================
-- SelahFinance · V8 · Roles (ADMIN / PASTOR / HERMANO) e iglesias
--
--   ADMIN    gestiona iglesias y cuentas. No tiene hogar ni ve finanzas.
--   PASTOR   pertenece a una iglesia; ve totales anónimos de su congregación
--            y el reporte de los hermanos que decidieron compartirlo.
--   HERMANO  usuario normal: sus finanzas y hábitos personales.
-- =====================================================================

CREATE TABLE iglesia (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(120) NOT NULL,
    ciudad      VARCHAR(80),
    distrito    VARCHAR(80),
    activa      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_iglesia_nombre_ciudad ON iglesia (lower(nombre), lower(coalesce(ciudad, '')));
CREATE TRIGGER trg_iglesia_updated BEFORE UPDATE ON iglesia
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Roles: USUARIO -> HERMANO, nuevo PASTOR
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS usuario_rol_check;
UPDATE usuario SET rol = 'HERMANO' WHERE rol = 'USUARIO';
ALTER TABLE usuario ADD CONSTRAINT usuario_rol_check CHECK (rol IN ('ADMIN', 'PASTOR', 'HERMANO'));
ALTER TABLE usuario ALTER COLUMN rol SET DEFAULT 'HERMANO';

-- Iglesia como entidad (reemplaza el texto libre iglesia_local)
ALTER TABLE usuario ADD COLUMN iglesia_id UUID REFERENCES iglesia (id);
ALTER TABLE usuario DROP COLUMN iglesia_local;
CREATE INDEX ix_usuario_iglesia_rol ON usuario (iglesia_id, rol) WHERE estado = 'ACTIVO';

-- Todo pastor y hermano pertenece a una iglesia (NOT VALID: no revalida filas previas)
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_iglesia
    CHECK (rol = 'ADMIN' OR iglesia_id IS NOT NULL) NOT VALID;

-- Cuentas creadas por el admin llevan contraseña temporal
ALTER TABLE usuario ADD COLUMN debe_cambiar_password BOOLEAN NOT NULL DEFAULT FALSE;

-- Consentimiento: el hermano decide compartir su reporte con su pastor.
-- NULL = no comparte; con fecha = comparte desde ese momento (queda evidencia).
ALTER TABLE usuario ADD COLUMN comparte_reporte_desde TIMESTAMPTZ;
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_comparte
    CHECK (comparte_reporte_desde IS NULL OR rol = 'HERMANO');
