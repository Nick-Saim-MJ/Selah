# Base de datos

| Qué | Dónde |
|---|---|
| Crear usuario y BD | [`scripts/00_crear_bd_y_usuario.sql`](scripts/00_crear_bd_y_usuario.sql) |
| Esquema (tablas, vistas, semillas) | [`backend/src/main/resources/db/migration`](../backend/src/main/resources/db/migration) — Flyway lo aplica al arrancar |
| Diagrama y reglas | [`docs/base-de-datos.md`](../docs/base-de-datos.md) |

```bash
psql -U postgres -f database/scripts/00_crear_bd_y_usuario.sql
```

Reglas para el equipo:
- **Nunca** modificar una migración ya aplicada; crear `V10__descripcion.sql`, `V11__...`, etc. (la última aplicada es V9)
- Probar una migración nueva levantando el backend contra una BD vacía (`mvn spring-boot:run`).
- Consultas útiles:
  ```sql
  SELECT * FROM v_resumen_mensual WHERE hogar_id = '<uuid>';     -- Inicio: saldo, apartados
  SELECT * FROM v_presupuesto_vs_real WHERE hogar_id = '<uuid>'; -- Reportes
  SELECT * FROM v_meta_progreso WHERE hogar_id = '<uuid>';       -- Metas
  SELECT tipo_evento, estado, count(*) FROM evento_outbox GROUP BY 1, 2;
  ```

Datos de demostración: **no** están en las migraciones. Los siembra el backend con `SPRING_PROFILES_ACTIVE=dev,demo`
(ver [docs/guia-demo-movil.md](../docs/guia-demo-movil.md)).
