# SelahFinance · App móvil (Flutter)

Clean Architecture por features + BLoC. Detalle en [../docs/arquitectura.md](../docs/arquitectura.md).

```bash
flutter pub get
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080   # emulador Android
flutter run --dart-define=API_BASE_URL=http://127.0.0.1:8080  # celular por USB (tras `adb reverse tcp:8080 tcp:8080`)
flutter test
flutter analyze
```

| `--dart-define` | Uso |
|---|---|
| `API_BASE_URL` | Backend SelahFinance (por defecto `http://10.0.2.2:8080`) |
| `API_SALUD_URL`, `API_IGLESIA_URL` | APIs de otros equipos (opcionales; ver `core/network/external_api_registry.dart`) |

Para crear una feature nueva, copia la estructura de `lib/features/movimientos/` (data → domain → presentation),
registra sus dependencias en `core/di/injection_container.dart` y su ruta en `core/router/app_router.dart`.

Nota: `google_fonts` está fijado en `^8.0.0` porque la 9.x usa `package:material_ui`, aún incompatible con
`flutter/material` en Flutter 3.47. Antes de producción conviene empaquetar Epilogue como asset
(`GoogleFonts.config.allowRuntimeFetching = false`) para no descargar fuentes en tiempo de ejecución.

Demo en celular real: [../docs/guia-demo-movil.md](../docs/guia-demo-movil.md).
Las pruebas de `test/contrato/` usan respuestas reales del backend guardadas en `test/fixtures/`.
