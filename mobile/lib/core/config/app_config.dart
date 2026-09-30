/// Configuración por entorno vía `--dart-define`, sin tocar código:
///
/// ```bash
/// flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080
/// ```
///
/// Las APIs de otros equipos se declaran igual (`API_<NOMBRE>_URL`) y se
/// registran en `ExternalApiRegistry`.
abstract final class AppConfig {
  /// Backend SelahFinance. 10.0.2.2 = localhost de la PC visto desde el emulador Android.
  static const apiBaseUrl = String.fromEnvironment('API_BASE_URL', defaultValue: 'http://10.0.2.2:8080');

  static const connectTimeout = Duration(seconds: 10);
  static const receiveTimeout = Duration(seconds: 20);

  /// APIs externas de módulos desarrollados por otros equipos (vacías = deshabilitadas).
  static const externalApis = <String, String>{
    'salud': String.fromEnvironment('API_SALUD_URL'),
    'iglesia': String.fromEnvironment('API_IGLESIA_URL'),
  };
}
