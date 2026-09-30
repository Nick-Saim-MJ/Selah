-- =====================================================================
-- Crea el usuario y la base de datos de SelahFinance (ejecutar UNA vez
-- como superusuario):   psql -U postgres -f database/scripts/00_crear_bd_y_usuario.sql
--
-- Las TABLAS no se crean aquí: las crea Flyway al arrancar el backend
-- (backend/src/main/resources/db/migration). Cambia la contraseña en
-- entornos compartidos y pásala al backend con DB_PASSWORD.
-- =====================================================================

CREATE ROLE selah WITH LOGIN PASSWORD 'selah';

CREATE DATABASE selah_finance
    WITH OWNER = selah
         ENCODING = 'UTF8'
         TEMPLATE = template0;

\connect selah_finance

-- citext es "trusted" desde PG13, pero se crea aquí por si el rol no tiene permiso
CREATE EXTENSION IF NOT EXISTS citext;
GRANT ALL ON SCHEMA public TO selah;
