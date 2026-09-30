# Integración con módulos de otros equipos

El sistema está preparado para **tres direcciones** de integración:

| Dirección | Mecanismo | Dónde está |
|---|---|---|
| A. Ellos consumen nuestra API | REST `/api/v1/integraciones/**` + header `X-API-Key` | `integraciones/infrastructure/security/ApiKeyAuthenticationFilter` |
| B. Nosotros consumimos sus APIs | Puerto de salida + adaptador HTTP (`RestClient`) | `reportes/application/port/out/MetricasDimensionPort` → `integraciones/infrastructure/adapter/HttpMetricasDimensionAdapter` |
| C. Les avisamos de eventos | Patrón Outbox → webhooks firmados | tabla `evento_outbox`, `suscripcion_webhook` |

En la app móvil, las APIs externas se registran en `core/network/external_api_registry.dart`
(`--dart-define=API_<NOMBRE>_URL=...`); si no están configuradas, la feature se oculta sin romper la app.

## Contrato general

- Base: `/api/v1`. Cambios incompatibles → `/api/v2` (la v1 se mantiene mientras haya consumidores).
- JSON, fechas ISO-8601 (`2026-09-28`), montos como número con 2 decimales, IDs UUID.
- Errores **RFC 9457** (`application/problem+json`):
  ```json
  {"type":"https://selahfinance.com/errores/CATEGORIA_REQUERIDA","title":"Regla de negocio incumplida",
   "status":422,"detail":"Un gasto necesita una categoría","codigo":"CATEGORIA_REQUERIDA"}
  ```
- Contrato vivo en Swagger: `/swagger-ui.html` → grupo **integraciones**. JSON: `/v3/api-docs/integraciones`
  (sirve para generar clientes con OpenAPI Generator).

## A. Consumir nuestra API (otros equipos)

1. Solicitar una API key al **administrador** de SelahFinance. Él la crea en la app (pestaña *Integraciones*) o por
   `POST /api/v1/admin/clientes-api` (`{"nombre","responsableEmail","scopes":[...]}`) y la clave `sf_<8 hex>.<64 hex>` se muestra
   **una sola vez** (en la BD solo queda su hash SHA-256). Se puede revocar con `DELETE /api/v1/admin/clientes-api/{id}`.
2. Probarla:
   ```bash
   curl -H "X-API-Key: sf_ab12cd34.<secreto>" http://localhost:8080/api/v1/integraciones/ping
   ```
   → `{"cliente":"modulo-salud","scopes":["habitos:write"],"hora":"..."}`
3. Los scopes se exponen como authorities `SCOPE_<scope>` (`@PreAuthorize("hasAuthority('SCOPE_habitos:write')")`).
4. **Idempotencia:** al crear recursos, enviar `referenciaExterna` (su ID). Reintentos no duplican.

Scopes concedibles: `movimientos:read`, `movimientos:write`, `habitos:write`, `reportes:read`.

> **Estado real:** hoy el único endpoint de datos bajo `/api/v1/integraciones/**` es `GET /ping`. Los scopes ya se validan y
> se convierten en authorities, pero los endpoints que permitan a otro equipo *escribir* movimientos o hábitos aún no se han
> expuesto (siguiente fase).

## B. Proveer métricas de Tiempo / Talento / Templo (contrato que esperamos)

Si su módulo mide hábitos (ej. ejercicio, horas de servicio, culto personal), expongan:

```
GET {base_url}/api/v1/mayordomia/metricas?usuarioId={suIdDeUsuario}&dimension=TEMPLO&desde=2026-09-01&hasta=2026-09-30
X-API-Key: <clave que nos den>
```

Respuesta `200`:
```json
[
  { "codigo": "EJERCICIO", "metaPeriodo": 900, "valorRegistrado": 640 },
  { "codigo": "AGUA",      "metaPeriodo": 240, "valorRegistrado": 210 }
]
```

- `dimension`: `TIEMPO` | `TALENTO` | `TEMPLO`. `codigo`: preferir los del catálogo (`habito_mayordomia.codigo`, V7).
- Registro del lado SelahFinance:
  ```sql
  INSERT INTO proveedor_externo (codigo, nombre, dimension, base_url, tipo_auth, timeout_ms)
  VALUES ('salud', 'Módulo de Salud', 'TEMPLO', 'https://salud.ejemplo.com', 'API_KEY', 5000);
  INSERT INTO vinculo_usuario_externo (usuario_id, proveedor_id, id_externo) VALUES (...);
  ```
  Secreto en variable de entorno: `SELAH_PROVEEDOR_SALUD_SECRET=<clave>`.
- Si su API falla o tarda más que `timeout_ms`, el reporte se genera igual con las demás fuentes (se registra en el log).

## C. Eventos (webhooks)

Cada cambio de negocio se guarda en `evento_outbox` en la misma transacción. Eventos actuales:

| Evento | Cuándo | Payload |
|---|---|---|
| `movimiento.registrado` | Se crea un movimiento | `movimientoId, hogarId, registradoPor, tipoMovimiento, monto, fecha, origen, ocurridoEn` |
| `movimiento.eliminado` | Borrado lógico | `movimientoId, hogarId, tipoMovimiento, monto, ocurridoEn` |

Además existe `hogar.creado`. Una suscripción puede pedir un evento concreto o `*` (todos).

**Administración (solo ADMIN):** `GET/POST /api/v1/admin/webhooks`, `DELETE /api/v1/admin/webhooks/{id}`
(`{"clienteApiId","tipoEvento","url"}`). El **secreto** `whsec_…` se muestra una sola vez y se guarda cifrado (AES-GCM).

**Entrega:** un job publica el outbox cada `SELAH_WEBHOOKS_INTERVALO_MS` (15 s por defecto) con `POST` JSON y cabeceras:

| Cabecera | Contenido |
|---|---|
| `X-Selah-Evento` | tipo, p. ej. `movimiento.registrado` |
| `X-Selah-Evento-Id` | UUID del evento (úsenlo para **idempotencia**: la entrega es *al menos una vez*) |
| `X-Selah-Firma` | `sha256=<HMAC-SHA256 del cuerpo con el secreto>` — verifíquenla antes de confiar en el aviso |

- Se reintenta hasta 5 veces; luego el evento pasa a `FALLIDO`. Responder 2xx = recibido.
- Un webhook nuevo solo recibe eventos ocurridos **desde** su creación (no el historial).
- Seguridad: no se siguen redirecciones y se bloquean destinos de red interna (loopback/privada) salvo en desarrollo
  (`SELAH_WEBHOOKS_RED_LOCAL=true`, activo solo en el perfil `dev`).
