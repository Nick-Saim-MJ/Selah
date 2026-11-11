# Despliegue en Render (API + PostgreSQL)

```
 APK ──HTTPS──► Render: web service "selah-api" (Docker, backend/Dockerfile) ──► Render: PostgreSQL "selah-db"
```

## 1. Qué se sube
Todo va en tu repositorio de GitHub (rama `main`). Para Render hacen falta estos archivos, que ya están en el proyecto:

| Archivo | Para qué |
|---|---|
| `backend/Dockerfile` | Cómo compilar y ejecutar la API (Java 17, memoria acotada para 512 MB) |
| `render.yaml` (raíz) | Blueprint: crea la API y la base con sus variables |
| `backend/src/main/resources/application.yml` | Acepta `DB_HOST`, `DB_PORT` y `DB_NAME` (Render no entrega una URL JDBC) |

```powershell
cd "D:\CICLO 9\FC"
git add render.yaml backend docs mobile README.md
git commit -m "Despliegue en Render"
git push
```

## 2. Crear los servicios (con el Blueprint)
1. Entra a <https://dashboard.render.com> con tu cuenta de GitHub y autoriza a Render a leer el repositorio.
2. **New + → Blueprint** → elige el repositorio → rama `main`.
3. Render lee `render.yaml` y muestra dos recursos: **selah-api** (web) y **selah-db** (PostgreSQL). Pulsa **Apply / Deploy Blueprint**.
4. Espera el primer build (10–15 min en el plan gratuito).

Las variables (`JWT_SECRET`, `SELAH_CLAVE_CIFRADO`, conexión a la base) las rellena Render solo; no copies secretos a ningún lado.

## 3. Verificar
En **selah-api → Logs** debe aparecer `Successfully applied 9 migrations`, `Demo lista…` y `Started SelahFinanceApplication`. Tu URL es `https://selah-api-XXXX.onrender.com` (arriba en el servicio):

```powershell
curl https://TU-URL.onrender.com/actuator/health
```

## 4. Generar el APK
```powershell
cd "D:\CICLO 9\FC\mobile"
D:\flutter\bin\flutter.bat build apk --release --dart-define=API_BASE_URL=https://TU-URL.onrender.com
```

## 5. Limitaciones del plan gratuito
- **La API se duerme** tras ~15 min sin tráfico; la primera petición tarda 1–2 min (los testers verán «No se pudo conectar» si no esperan). Conviene abrir `/actuator/health` unos minutos antes de una demo.
- **512 MB de RAM y CPU compartida:** el arranque es lento. Si el log muestra `Out of memory`, baja `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE` a 3.
- **La base gratuita caduca a los 30 días** y se elimina (con los datos). Para pruebas largas, pasa a un plan de pago o crea otra y vuelve a sembrar.
- Con `SPRING_PROFILES_ACTIVE=demo`, cualquiera con la URL puede entrar como `admin@selah.demo` / `Demo1234!`. Úsalo solo con datos ficticios.

## 6. Sin datos de demo
En **selah-api → Environment**: `SPRING_PROFILES_ACTIVE=prod`, y añade `SELAH_ADMIN_EMAIL` y `SELAH_ADMIN_PASSWORD` (temporal, se cambia al entrar). Entra como admin y crea una iglesia antes de invitar a nadie. Si la base ya tenía la demo, bórrala (*selah-db → ⋯ → Delete*) y vuelve a aplicar el Blueprint.

## 7. Problemas frecuentes
| Síntoma | Causa | Solución |
|---|---|---|
| `Connection refused` / `UnknownHost` | API y base en regiones distintas | Misma región en ambos (`region` en `render.yaml`) |
| `JWT_SECRET debe tener al menos 32 bytes` | Variable ausente | Ver que exista en *Environment* |
| `No open ports detected` | Arranque lento, Render no ve el puerto a tiempo | Reintentar con *Manual Deploy*; el arranque suele pasar la segunda vez |
| El APK no conecta | URL equivocada o API dormida | Probar `/actuator/health` en el navegador del celular |
