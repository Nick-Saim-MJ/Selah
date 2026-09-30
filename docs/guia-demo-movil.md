# Guía de demostración en tu celular (depuración USB)

Esta guía te lleva de cero a **mostrar la app en tu teléfono Android** conectado por cable a tu PC, con datos de
demostración listos. Tiempo estimado la primera vez: 30–40 min (la mayoría es descargar/compilar). Las siguientes
veces: 5 min.

> **Solo Android.** Un iPhone requiere una Mac con Xcode; no se puede desde Windows.

Cómo funciona (para que entiendas qué estás armando):

```
 Celular (app Flutter)  ──USB──►  adb reverse  ──►  PC: backend Spring Boot :8080  ──►  PostgreSQL :5432
   127.0.0.1:8080  (cable, sin depender del Wi-Fi)
```

`adb reverse` hace que el `127.0.0.1:8080` **del teléfono** llegue al `8080` **de tu PC**. Por eso no necesitas
Wi-Fi, ni conocer la IP de tu PC, ni tocar el firewall.

---

## 0. Lista rápida antes de empezar

| ✔ | Requisito | Estado en tu PC (verificado) |
|---|---|---|
| ☐ | Cable USB **de datos** (no solo de carga) | — |
| ☐ | Android 5.0+ con «Opciones de desarrollador» | ver paso 1 |
| ☐ | Flutter 3.47 (`D:\flutter`) | ✅ instalado |
| ☐ | Android SDK + `adb` | ✅ en `C:\Users\AORUS\AppData\Local\Android\Sdk\platform-tools` |
| ☐ | **Licencias de Android aceptadas** | ⚠️ **pendiente — debes aceptarlas tú** (paso 2) |
| ☐ | PostgreSQL 14 **encendido** | ⚠️ **hoy NO está corriendo** en el puerto 5432 (paso 3) |
| ☐ | Java 17 + Maven | ✅ instalados |

---

## 1. Activar la depuración USB en el celular

1. **Ajustes → Acerca del teléfono** → toca **7 veces** «Número de compilación» (en Xiaomi/Redmi/POCO: «Versión de MIUI/HyperOS»;
   en Samsung: «Información de software»). Aparece «Ahora eres desarrollador».
2. **Ajustes → Sistema → Opciones de desarrollador** (en Samsung/Xiaomi puede estar en «Ajustes adicionales»).
3. Activa **Depuración por USB**.
   - Xiaomi/Redmi/POCO: activa también **«Instalar vía USB»** y **«Depuración USB (ajustes de seguridad)»** (pide iniciar sesión con cuenta Mi).
   - Otros fabricantes: si existe **«Instalar apps vía USB»**, actívalo.
   - Huawei/Honor: activa también **«Permitir depuración ADB en modo solo carga»** y, si aparece, rechaza/ignora el aviso de HiSuite.
     Si no lista el equipo, elige en el aviso del teléfono el modo «Transferir archivos».
4. Conecta el cable. En el celular elige el modo USB **«Transferencia de archivos (MTP)»** (no «Solo carga»).
5. Aparece **«¿Permitir depuración USB?»** → marca «Permitir siempre desde este equipo» → **Permitir**.

**Windows y drivers:** con Samsung/Xiaomi/Motorola normalmente basta con el driver que Windows instala solo. Si no aparece el
dispositivo en el paso siguiente, instala el driver USB del fabricante (Samsung: «Samsung USB Driver»; Xiaomi: «Mi USB Driver»)
o «Google USB Driver» desde el SDK Manager.

### Comprobar que la PC ve el teléfono

Abre PowerShell y usa la ruta completa de `adb` (o agrégala al PATH, ver «Trucos»):

```powershell
& "C:\Users\AORUS\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices
```

Debe salir tu equipo con estado **`device`**:

```
List of devices attached
R58M12ABCDE     device
```

| Estado | Qué significa | Solución |
|---|---|---|
| (lista vacía) | La PC no ve el teléfono | Otro cable/puerto, modo MTP, instalar driver |
| `unauthorized` | No aceptaste el diálogo en el celular | Desbloquea el teléfono y acepta; si no sale: Opciones de desarrollador → «Revocar autorizaciones de depuración USB» y reconecta |
| `offline` | Conexión colgada | `adb kill-server` y reconecta el cable |

---

## 2. Aceptar las licencias de Android (una sola vez) ⚠️

Yo **no** las acepté por ti: son términos legales de Google y deben aceptarse por una persona. En PowerShell:

```powershell
D:\flutter\bin\flutter.bat doctor --android-licenses
```

Lee y responde `y` a cada pregunta. Luego verifica:

```powershell
D:\flutter\bin\flutter.bat doctor -v
```

En la sección **Android toolchain** no debe quedar «Android license status unknown». (Aun sin aceptarlas, en esta PC el APK
de depuración se compiló bien, pero acéptalas para evitar sorpresas en la presentación.)

---

## 3. Encender la base de datos (PostgreSQL) ⚠️

Tu PostgreSQL 14 está instalado pero **apagado**. Enciéndelo con **una** de estas opciones:

**A) Servicio de Windows (lo más simple).** Win + R → `services.msc` → busca **postgresql-x64-14** → clic derecho → *Iniciar*.
(Opcional: *Propiedades → Tipo de inicio: Automático* para que arranque solo.)

**B) Docker** (si lo tienes): en la raíz del proyecto

```powershell
docker compose up -d
```

### Crear la base y el usuario (solo la primera vez)

```powershell
psql -U postgres -f database\scripts\00_crear_bd_y_usuario.sql
```

Crea el usuario `selah` / contraseña `selah` y la base `selah_finance` (los valores por defecto que usa el backend en
desarrollo). Si `psql` no se reconoce, usa la ruta completa, p. ej. `& "C:\Program Files\PostgreSQL\14\bin\psql.exe" -U postgres -f ...`.
Las **tablas** las crea el backend solo (Flyway) al arrancar.

---

## 4. Arrancar el backend con datos de demostración

En una terminal (déjala abierta durante toda la demo):

```powershell
cd "D:\CICLO 9\FC\backend"
$env:SPRING_PROFILES_ACTIVE = "dev,demo"
mvn spring-boot:run
```

El perfil `demo` siembra automáticamente (solo si la base está vacía):

- 2 iglesias: **Adventista Central** y **Adventista Miraflores**
- 1 administrador, 1 pastor y 6 hermanos con 5 meses de historial, metas, una deuda, hábitos y reportes 4T ya generados.

Espera a ver en el log: **`Demo lista: 2 iglesias, 1 admin, 1 pastor, 6 hermanos…`** (≈30 s).
Comprobación rápida en el navegador de la PC: <http://localhost:8080/actuator/health> → `{"status":"UP"}`
y la documentación de la API en <http://localhost:8080/swagger-ui.html>.

> Nunca uses el perfil `demo` en un servidor real: crea cuentas con contraseña conocida.

### Cuentas de demostración

Contraseña de **todas**: `Demo1234!`

| Rol | Email | Qué verás |
|---|---|---|
| **Hermano** (protagonista) | `ana@selah.demo` | Datos completos; diezmo de este mes **pendiente** (para mostrar «Marcar como entregado»); comparte su reporte con el pastor |
| Hermano | `luis@selah.demo`, `marta@selah.demo` | Comparten su reporte con el pastor |
| Hermano | `jose@selah.demo`, `rosa@selah.demo`, `carlos@selah.demo` | No comparten (Carlos es de otra iglesia) |
| **Pastor** | `pastor@selah.demo` | Iglesia Central: totales anónimos + quienes comparten su reporte |
| **Administrador** | `admin@selah.demo` | Panel de cuentas, iglesias e integraciones (sin acceso a finanzas) |

---

## 5. Conectar el teléfono al backend y ejecutar la app

Con el teléfono conectado (`adb devices` → `device`):

```powershell
# 1) Túnel: el 8080 del teléfono apunta al 8080 de la PC
& "C:\Users\AORUS\AppData\Local\Android\Sdk\platform-tools\adb.exe" reverse tcp:8080 tcp:8080

# 2) Compilar, instalar y abrir la app en el teléfono
cd "D:\CICLO 9\FC\mobile"
D:\flutter\bin\flutter.bat run --dart-define=API_BASE_URL=http://127.0.0.1:8080
```

La primera compilación tarda 3–8 min (descarga dependencias de Gradle). Las siguientes, segundos. Durante `flutter run`:
`r` = hot reload, `R` = hot restart, `q` = salir.

> ⚠️ **`adb reverse` se pierde** si desconectas el cable o reinicias `adb`. Si la app dice «No se pudo conectar»,
> repite el paso 1.

### Alternativa: instalar el APK (sin dejar `flutter run` abierto)

Útil si quieres dejar la app instalada y presentar sin la terminal de Flutter:

```powershell
cd "D:\CICLO 9\FC\mobile"
D:\flutter\bin\flutter.bat build apk --debug --dart-define=API_BASE_URL=http://127.0.0.1:8080
& "C:\Users\AORUS\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r build\app\outputs\flutter-apk\app-debug.apk
```

Con el APK sigues necesitando el `adb reverse tcp:8080 tcp:8080` de arriba (o usar la alternativa por Wi-Fi).

### Alternativa por Wi-Fi (sin cable durante la presentación)

1. PC y teléfono en la **misma red Wi-Fi** (no vale «red de invitados»).
2. IP de la PC: `ipconfig` → «Dirección IPv4» (p. ej. `192.168.1.50`).
3. Permite el puerto en el firewall (PowerShell **como administrador**):
   `New-NetFirewallRule -DisplayName "Selah demo 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow`
4. Compila con `--dart-define=API_BASE_URL=http://192.168.1.50:8080`.

Tras la demo, elimina la regla: `Remove-NetFirewallRule -DisplayName "Selah demo 8080"`.

### Verificación automática (opcional)

```powershell
powershell -ExecutionPolicy Bypass -File "D:\CICLO 9\FC\scripts\verificar-demo.ps1"
```

Revisa: teléfono conectado y autorizado, túnel activo, backend respondiendo y login de demo. Te dice qué falta.

---

## 6. Guion sugerido de la presentación (≈12 min)

Inicia sesión con cada cuenta. Para cambiar de rol: toca tu **inicial (arriba a la derecha) → Perfil y configuración → Cerrar sesión** (el administrador tiene «Cerrar sesión» en el menú de arriba a la derecha).

### A. Hermano — `ana@selah.demo` (6 min)
1. **Inicio:** saludo, **semáforo verde** («tu nivel de gasto es adecuado»), *saldo disponible S/ 830* con la nota «Diezmo y ofrenda ya
   apartados: S/ 540», ingresos vs. gastos, **diezmo pendiente S/ 450**, meta principal (Fondo de emergencia 65 %).
2. **Movimientos → +:** registra un gasto (p. ej. Alimentación S/ 50). Vuelve a Inicio: el saldo y el semáforo cambian
   (**una sola fuente de verdad**). Toca un movimiento para ver el detalle y bórralo si quieres.
3. **Diezmos:** desglose del ingreso (diezmo 10 %, ofrenda 2 %), historial de 6 meses → **«Marcar como entregado»** →
   el pendiente desaparece del Inicio.
4. **Metas:** Fondo de emergencia y Enganche de auto, con aporte mensual sugerido; **deudas** con cuota, interés y meses restantes.
5. **Reportes → Finanzas:** presupuestado vs. real por categoría (Alimentación +11,4 % en rojo). **→ Mayordomía 4T:** puntaje integral
   y las cuatro dimensiones **Tiempo, Talento, Tesoro, Templo**; despliega cada una para ver los hábitos que la componen.
6. **Hábitos (tarjeta en Inicio o Perfil → «Mis hábitos de hoy»):** marca oración, estudio bíblico, voluntariado… y vuelve a Reportes para ver que el puntaje 4T se mueve.
7. **Campana:** bandeja de avisos dentro de la app (sin Firebase).
8. **Perfil y configuración:** tu iglesia y el interruptor **«Compartir mi reporte con mi pastor»** (también aparece al final de Reportes → Mayordomía 4T) — el hermano decide. Ahí mismo: mayordomía (porcentajes, Modo Sábado), fuentes de ingreso, categorías y preferencias de avisos.

### B. Pastor — `pastor@selah.demo` (2 min)
- Icono de congregación (arriba): **hermanos activos, cuántos tienen reporte, % de diezmo al día, promedio 4T**.
  Mensaje clave: *son totales anónimos y no incluyen dinero*.
- **«Hermanos que comparten su reporte contigo»:** solo Ana, Luis y Marta (los que dieron permiso). Se ve nombre, puntajes 4T, semáforo,
  hábitos y **si cumplió el diezmo (sí/no)**. **Nunca montos.**
- Desactiva «compartir» en Ana y vuelve a entrar como pastor: Ana ya no aparece. Si quedaran menos de 3 hermanos con datos,
  los promedios se ocultan (anonimato mínimo).

### C. Administrador — `admin@selah.demo` (4 min)
- **Resumen:** cuentas por rol; aviso de privacidad: *el administrador no ve montos ni reportes de nadie*.
- **Usuarios:** buscar, filtrar por rol/bloqueadas; **Nueva cuenta** (pastor o hermano) → entrega una **contraseña temporal** que se
  muestra **una sola vez**; la persona debe cambiarla al primer ingreso. Bloquear una cuenta la **expulsa al instante**
  (aunque tenga sesión abierta). Cambiar de rol invalida sus sesiones.
- **Iglesias:** crear/editar/desactivar.
- **Integraciones (para los equipos compañeros):** crear un **cliente de API** (la clave `sf_…` se muestra **una vez**) y un **webhook**
  (secreto `whsec_…` una vez, eventos firmados con HMAC). Ver `docs/integracion-apis.md`.

### D. Registro de una persona nueva (2 min)
Cierra sesión → **«Soy nuevo: crear una cuenta»** → completa datos y **elige su iglesia** → asistente de 4 pasos (Bienvenido → Tus ingresos → Tu mayordomía → Tus gastos) → entra a su Inicio ya con su hogar creado.

---

## 7. Mostrar el Modo Sábado y la reflexión semanal (si presentas entre semana)

Son funciones de **viernes 18:00 a sábado 18:00** (hora de Lima). Para mostrarlas hoy, arranca el backend con un **reloj simulado**:

```powershell
cd "D:\CICLO 9\FC\backend"
$env:SPRING_PROFILES_ACTIVE = "dev,demo"
$env:SELAH_DEMO_RELOJ_SIMULADO = "2026-10-03T00:30:00Z"   # = viernes 2-oct-2026, 19:30 hora de Lima
mvn spring-boot:run
```

| Valor de `SELAH_DEMO_RELOJ_SIMULADO` | Hora simulada (Lima) | Qué muestra |
|---|---|---|
| `2026-10-02T21:00:00Z` | Viernes 16:00 | **Reflexión semanal visible** (desde el viernes 15:00); aún no es Modo Sábado |
| `2026-10-03T00:30:00Z` | Viernes 19:30 | **Modo Sábado:** los avisos de consumo (p. ej. «presupuesto excedido») **se posponen** hasta el sábado 18:00; solo llega el recordatorio de diezmo. Reflexión visible |

- El reloj simulado **sigue corriendo** desde esa hora.
- Solo funciona en los perfiles `dev`/`demo` (en producción el backend se niega a arrancar con esta variable).
- **Importante:** define el reloj **antes de crear los datos** (base vacía, ver «Reiniciar datos»). Así los datos de demo quedan en el
  mes simulado y todo cuadra. Si ya sembraste con la fecha real, reinicia los datos.
- En la app: **Reportes → Mayordomía 4T** y la tarjeta de **Reflexión** en Inicio; en **Perfil y configuración → Mayordomía** está el interruptor **Modo Sábado**, y en las preferencias de avisos «Invitación a la reflexión del viernes».

---

## 8. Reiniciar los datos de demostración

Para volver al estado inicial (por ejemplo antes de cada presentación):

1. Detén el backend (`Ctrl + C`).
2. Recrea la base:

   ```powershell
   psql -U postgres -c "DROP DATABASE selah_finance WITH (FORCE);" -c "CREATE DATABASE selah_finance OWNER selah;"
   psql -U postgres -d selah_finance -c "CREATE EXTENSION IF NOT EXISTS citext;" -c "GRANT ALL ON SCHEMA public TO selah;"
   ```
3. Arranca el backend otra vez (paso 4): vuelve a sembrar todo.

---

## 9. Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| La app dice «No se pudo conectar…» | Túnel caído o backend apagado | `adb reverse tcp:8080 tcp:8080`; revisa `http://localhost:8080/actuator/health` |
| `adb devices` no lista el teléfono | Cable solo de carga / modo USB / driver | Cable de datos, modo MTP, driver del fabricante |
| `unauthorized` | Falta aceptar el diálogo RSA | Ver tabla del paso 1 |
| El backend no arranca: `Connection refused localhost:5432` | PostgreSQL apagado | Paso 3 |
| `password authentication failed for user "selah"` | No se ejecutó el script SQL, o cambiaste la contraseña | Paso 3, o `DB_USER`/`DB_PASSWORD` |
| `Port 8080 already in use` | Otro programa usa el 8080 | Cierra el otro, o `$env:PORT=8081` y usa `tcp:8081` y `API_BASE_URL=http://127.0.0.1:8081` |
| `flutter run`: «Waiting for another flutter command to release the startup lock» | Otro Flutter/VS Code corriendo | Cierra el otro proceso, o borra `D:\flutter\bin\cache\lockfile` |
| Gradle: «Timeout waiting to lock … / Could not create service of type…» | El plugin de Java de VS Code usa Gradle a la vez | Cierra VS Code y reintenta; luego `flutter clean` si persiste |
| «Android license status unknown» | Licencias sin aceptar | Paso 2 |
| Build lento o falla en `kotlin` con rutas de otra unidad | El proyecto está en `D:` y la caché en `C:` | Ya está mitigado (`kotlin.incremental=false`); reintenta |
| En Xiaomi, la instalación falla con `INSTALL_FAILED_USER_RESTRICTED` | Falta «Instalar vía USB» | Actívalo (paso 1.3) |
| Login: «Email o contraseña incorrectos» | Datos de demo no sembrados (el perfil `demo` no estaba activo) | Reinicia con `SPRING_PROFILES_ACTIVE=dev,demo` sobre base vacía |
| La sesión se cierra sola | El token dura 12 h en dev | Vuelve a iniciar sesión |
| El teléfono se bloquea a mitad de la demo | Pantalla en reposo | Ajustes → Pantalla → tiempo de espera; o «Mantener pantalla encendida» en Opciones de desarrollador (con cable) |

### Trucos

- **`adb` en el PATH** (solo esta sesión de PowerShell):
  `$env:Path += ";C:\Users\AORUS\AppData\Local\Android\Sdk\platform-tools"` → luego basta `adb devices`.
- **Ver los logs de la app** en vivo: `adb logcat *:S flutter:V`.
- **Proyectar la pantalla del celular** en el proyector desde la PC (recomendado): instala **scrcpy**
  (`winget install Genymobile.scrcpy`) y ejecuta `scrcpy --stay-awake --turn-screen-off` (el teléfono sigue con el cable).
  Tip: `scrcpy --max-size 1280` si va lento.
- **Modo avión + cable** durante la presentación evita notificaciones ajenas; el túnel USB sigue funcionando.
- **Sube el brillo y activa «No molestar»** para que no salgan avisos de otras apps en pantalla compartida.

---

## 10. Lista final el día de la presentación

- [ ] Celular cargado, cable de datos, `adb devices` → `device`
- [ ] PostgreSQL encendido; backend `UP` con «Demo lista» en el log
- [ ] `adb reverse tcp:8080 tcp:8080` ejecutado (se repite si reconectas el cable)
- [ ] App abierta con `ana@selah.demo` y al menos una vez probado el flujo pastor y admin
- [ ] Si presentas entre semana y quieres el Modo Sábado: reloj simulado sobre **base recién sembrada** (paso 7)
- [ ] Internet no es necesario (todo es local); Wi-Fi opcional
- [ ] Plan B: el APK ya instalado en el teléfono y el backend levantado; y capturas de pantalla por si falla el cable
