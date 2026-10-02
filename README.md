# SelahFinance — Mayordomía cristiana

App de administración financiera con enfoque de mayordomía adventista: **Tiempo, Talento, Tesoro y Templo**.
Especificación funcional: [Especificacion_App_Mayordomia_Financiera.md](Especificacion_App_Mayordomia_Financiera.md).

## Estructura del repositorio

```
FC/
├── backend/        API REST · Spring Boot 4.1 · Java 17 · Maven · Clean Architecture (hexagonal por módulos)
├── mobile/         App · Flutter 3.47 · Dart 3.13 · Clean Architecture + BLoC por features
├── database/       Scripts de creación de BD/usuario y guía (el esquema vive en backend/…/db/migration)
├── docs/           Arquitectura, modelo de datos, integración con otros equipos, reporte 4T
└── docker-compose.yml   PostgreSQL 14 opcional (si no se usa el instalado localmente)
```

| Documento | Contenido |
|---|---|
| [docs/arquitectura.md](docs/arquitectura.md) | Capas, regla de dependencia, cómo agregar un módulo/feature |
| [docs/base-de-datos.md](docs/base-de-datos.md) | Diagrama ER, tablas, fórmula del saldo disponible |
| [docs/integracion-apis.md](docs/integracion-apis.md) | Cómo otros equipos consumen nuestra API y cómo consumimos las suyas |
| [docs/reporte-4T.md](docs/reporte-4T.md) | Dimensiones, hábitos y algoritmo de puntaje |

## Puesta en marcha

### 1. Base de datos (PostgreSQL 14+)

Con el PostgreSQL instalado (ejecutar como superusuario):

```bash
psql -U postgres -f database/scripts/00_crear_bd_y_usuario.sql
```

O con Docker: `docker compose up -d`. Las tablas las crea **Flyway** al arrancar el backend.

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html (grupos `app-movil` e `integraciones`)
- Variables: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (obligatoria fuera del perfil `dev`), `PORT`, `CORS_ORIGENES`.
- Pruebas (incluyen reglas de arquitectura con ArchUnit): `mvn test`

### 3. App móvil

```bash
cd mobile
flutter pub get
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080
```

`10.0.2.2` es el localhost de la PC visto desde el emulador Android; en web/escritorio usa `http://localhost:8080`.
Pruebas: `flutter test` · Análisis: `flutter analyze`.

## Roles

| Rol | Qué hace |
|---|---|
| **ADMIN** | Gestiona cuentas (pastores y hermanos), iglesias e integraciones. **No ve finanzas de nadie.** |
| **PASTOR** | Sus finanzas propias + vista de su iglesia: totales anónimos y reportes de quienes decidieron compartir (sin montos). |
| **HERMANO** | Usuario normal: se registra solo, elige su iglesia y decide si comparte su reporte con su pastor. |

## Demo en el celular

Guía paso a paso (depuración USB, datos de demostración, guion por rol, Modo Sábado con reloj simulado):
**[docs/guia-demo-movil.md](docs/guia-demo-movil.md)** · verificación rápida: `powershell -File scripts/verificar-demo.ps1`.
Backend con datos de demo: `SPRING_PROFILES_ACTIVE=dev,demo` (contraseña de las cuentas demo: `Demo1234!`).

## Backend en la nube y APK para testers

[docs/despliegue-railway.md](docs/despliegue-railway.md): despliegue con Docker en Railway (`backend/Dockerfile`), variables, APK de release apuntando a la URL pública y cómo compartirlo.

## Estado actual

Backend (Flyway V1–V9) y app móvil implementan: identidad con roles, iglesias, hogar y mayordomía, movimientos, diezmos/ofrendas,
categorías y presupuesto, metas y deudas, hábitos, reporte financiero y 4T, reflexión semanal, notificaciones en la app,
vista de pastor, panel de admin, e integraciones (API keys, webhooks firmados). Pruebas: `mvn test` (incluye ArchUnit) y `flutter test`
(incluye contrato app ↔ backend).

Pendiente: notificaciones push (Firebase), endpoints de datos para otros equipos (hoy solo `/api/v1/integraciones/ping` y webhooks salientes).
