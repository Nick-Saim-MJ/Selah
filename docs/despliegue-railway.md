# Despliegue del backend en la nube (Railway) y APK para testers

Objetivo: que los interesados instalen un APK en su celular y usen la app **sin tu PC encendida**.

```
 APK en los celulares ──HTTPS──►  Railway: servicio "api" (Docker, Spring Boot)  ──►  Railway: PostgreSQL
```

> Plan **Gratis** de Railway (según lo que viste): prueba de 30 días con US$ 5 de crédito, luego US$ 1/mes; hasta 1 vCPU / 0,5 GB
> de RAM por servicio; sin tarjeta. Alcanza para pruebas, con la RAM justa (el `Dockerfile` ya limita la memoria de Java).
> Revisa tu consumo en el panel de Railway: si se agota el crédito, el servicio se detiene.

---

## 1. Antes de empezar

- El proyecto debe estar en un repositorio de **GitHub** (lo subes tú). Railway despliega desde ahí.
- `backend/Dockerfile` ya está en el repo (compila con Maven y ejecuta con Java 17).
- Prepara dos secretos aleatorios (en PowerShell):

  ```powershell
  -join ((48..57)+(65..90)+(97..122) | Get-Random -Count 48 | ForEach-Object {[char]$_})
  ```

  Ejecútalo **dos veces**: uno será `JWT_SECRET` y el otro `SELAH_CLAVE_CIFRADO`. Guárdalos en un gestor de contraseñas.
  `JWT_SECRET` debe tener al menos 32 caracteres (el backend se niega a arrancar si es más corto).

## 2. Crear el proyecto en Railway

1. Entra a <https://railway.com/new> → **Deploy PostgreSQL**. Queda un servicio llamado `Postgres`.
2. En el mismo proyecto: **+ New → GitHub Repo** → elige el repositorio.
3. Abre el servicio nuevo → **Settings**:
   - **Root Directory:** `backend` (ahí está el Dockerfile).
   - **Healthcheck Path:** `/actuator/health`.
4. **Variables** del servicio de la API (pestaña *Variables* → *Raw Editor*, pega y reemplaza los dos secretos):

   ```
   SPRING_PROFILES_ACTIVE=demo
   JWT_SECRET=<secreto 1>
   SELAH_CLAVE_CIFRADO=<secreto 2>
   JWT_EXPIRACION=PT12H
   DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
   DB_USER=${{Postgres.PGUSER}}
   DB_PASSWORD=${{Postgres.PGPASSWORD}}
   ```

   - `${{Postgres.…}}` son *referencias* a las variables del servicio de base de datos. Si renombraste ese servicio, cambia `Postgres`.
   - No definas `PORT`: Railway la pone sola y el backend la lee.
   - `SPRING_PROFILES_ACTIVE=demo` siembra las cuentas y datos de demostración (ver sección 6 para la alternativa sin datos de demo).
5. **Settings → Networking → Generate Domain**. Obtienes algo como `https://selah-api-production.up.railway.app`.
6. Espera el despliegue (el primer build tarda 4–8 min). En **Deployments → View logs** debe aparecer
   `Started SelahFinanceApplication` y `Demo lista: 2 iglesias, 1 admin, 1 pastor, 6 hermanos…`.

## 3. Verificar

Reemplaza la URL por la tuya:

```powershell
curl https://TU-URL.up.railway.app/actuator/health
# {"status":"UP"}

curl -X POST https://TU-URL.up.railway.app/api/v1/auth/login -H "Content-Type: application/json" -d '{"email":"ana@selah.demo","password":"Demo1234!"}'
```

La documentación interactiva de la API queda en `https://TU-URL.up.railway.app/swagger-ui.html`.

## 4. Generar el APK para los testers

Apunta la app a la URL pública (HTTPS) y compila en **release** (más liviano y rápido que el de depuración):

```powershell
cd "D:\CICLO 9\FC\mobile"
D:\flutter\bin\flutter.bat build apk --release --dart-define=API_BASE_URL=https://TU-URL.up.railway.app
```

El archivo queda en `mobile\build\app\outputs\flutter-apk\app-release.apk`. Cámbiale el nombre, por ejemplo `selah-finance-pruebas.apk`.

- La URL queda **fija dentro del APK**: si cambias de dominio hay que recompilar.
- Este APK se firma con la llave de depuración (suficiente para pruebas). **No sirve para publicar en Play Store**: eso requiere
  una llave de firma propia y otro proceso.

## 5. Compartir con los interesados

1. Sube el APK a Google Drive (o envíalo por WhatsApp/Telegram) y comparte el enlace.
2. Cada tester, en su Android: abre el archivo → permitir **«Instalar apps desconocidas»** para esa app (Drive/Chrome/WhatsApp) → Instalar.
3. Ábrela y entra con una cuenta de demo (tabla de la guía de demo, contraseña `Demo1234!`) o con **«Soy nuevo: crear una cuenta»**.
4. Avísales que **la primera apertura puede tardar** unos segundos si el servicio estuvo inactivo, y que **no usen datos reales**.

Alternativa más formal: *Firebase App Distribution* (gratis; los invitas por correo y reciben un enlace de instalación).

## 6. Sin datos de demo (registro libre)

Si prefieres que cada persona se registre y no existan cuentas públicas conocidas:

1. En la API cambia `SPRING_PROFILES_ACTIVE=prod` (cualquier valor distinto de `demo`/`dev`).
2. Añade `SELAH_ADMIN_EMAIL=tu@correo.com` y `SELAH_ADMIN_PASSWORD=<contraseña temporal fuerte>`. Se crea un único administrador,
   que **debe cambiar la contraseña** en su primer ingreso.
3. Entra con ese admin y crea al menos una **iglesia** (pestaña *Iglesias*): sin iglesias, nadie puede registrarse.
4. Redeploy. Si ya habías sembrado la demo, limpia la base (sección 7).

## 7. Reiniciar los datos

En el servicio `Postgres` → pestaña **Data → Query** ejecuta:

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

Luego **Redeploy** de la API: Flyway recrea las tablas y, con el perfil `demo`, vuelve a sembrar todo.

## 8. Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| La app dice «No se pudo conectar» | URL equivocada en el APK o API caída | Abre `/actuator/health` en el navegador del celular; recompila el APK con la URL correcta |
| El deploy falla: `selah.seguridad.jwt-secret debe tener al menos 32 bytes` | `JWT_SECRET` corto o ausente | Usa un secreto de 48 caracteres |
| `Connection refused` / `password authentication failed` | Variables `DB_*` mal referenciadas | Revisa que el servicio se llame `Postgres` o ajusta las referencias |
| El servicio se reinicia solo (`Killed`, código 137) | Se agotó la memoria de 0,5 GB | Reduce carga (`SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=3`) o sube el plan |
| El primer login tarda mucho | Arranque en frío de Spring Boot | Normal tras inactividad; espera unos segundos |
| Sesión expirada a las 2 h | Falta `JWT_EXPIRACION` | Añade `JWT_EXPIRACION=PT12H` |
| El APK no se instala | Falta permiso de «apps desconocidas» o el equipo es de 32 bits | Habilita el permiso; si es 32 bits compila sin restringir la arquitectura (el comando de arriba ya es universal) |

## 9. Seguridad: léelo antes de invitar gente

- Con el perfil `demo`, **cualquiera con la URL** puede entrar como administrador (`admin@selah.demo` / `Demo1234!`). Úsalo solo con datos ficticios y por un tiempo limitado; luego apaga el servicio o pasa a la sección 6.
- Swagger (`/swagger-ui.html`) es público. Es útil para mostrar la API, pero puedes quitarlo antes de abrirlo al público general.
- Nunca subas `JWT_SECRET` ni `SELAH_CLAVE_CIFRADO` al repositorio: solo van en las variables de Railway.
- Si los testers van a registrar datos financieros reales, hace falta primero un aviso de privacidad y su consentimiento
  (los datos incluyen información sensible de la iglesia y de las personas).
