import 'package:dio/dio.dart';

import '../config/app_config.dart';
import 'api_client.dart';

/// Registro de APIs de otros equipos (módulos independientes).
///
/// Para integrar una API nueva:
/// 1. Declarar su URL en [AppConfig.externalApis] (`--dart-define=API_<NOMBRE>_URL=...`).
/// 2. Crear en la feature correspondiente un `*RemoteDataSource` que reciba
///    `registry.cliente('<nombre>')`.
/// 3. Si la API no está configurada, la feature debe degradar con elegancia
///    (ocultar la tarjeta, no romper la app).
class ExternalApiRegistry {
  ExternalApiRegistry({Map<String, String>? urls})
      : _urls = Map.unmodifiable(
          Map.of(urls ?? AppConfig.externalApis)..removeWhere((_, url) => url.isEmpty),
        );

  final Map<String, String> _urls;
  final Map<String, Dio> _clientes = {};

  bool disponible(String nombre) => _urls.containsKey(nombre);

  Iterable<String> get nombres => _urls.keys;

  /// Cliente HTTP para la API [nombre], o `null` si no está configurada.
  Dio? cliente(String nombre, {List<Interceptor> interceptores = const []}) {
    final url = _urls[nombre];
    if (url == null) return null;
    return _clientes.putIfAbsent(nombre, () => ApiClient.crear(baseUrl: url, interceptores: interceptores));
  }
}
