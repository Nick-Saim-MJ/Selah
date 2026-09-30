import 'dart:convert';

import 'package:dio/dio.dart';

/// Dio que responde con JSON fijo por ruta, sin red. Sirve para probar que la app entiende
/// EXACTAMENTE el formato que envía el backend (los JSON de las pruebas salen de respuestas reales).
class FakeDio {
  FakeDio(this.respuestas) {
    dio = Dio(BaseOptions(baseUrl: 'http://fake'))..httpClientAdapter = _Adaptador(this);
  }

  /// "METODO /ruta" → JSON (objeto o lista) o un [int] con el código de error.
  final Map<String, Object> respuestas;
  late final Dio dio;

  /// Peticiones recibidas ("GET /api/v1/x") y sus cuerpos, para verificar lo que envía la app.
  final List<String> peticiones = [];
  final List<Object?> cuerpos = [];
}

class _Adaptador implements HttpClientAdapter {
  _Adaptador(this._fake);

  final FakeDio _fake;

  @override
  Future<ResponseBody> fetch(RequestOptions options, Stream<List<int>>? requestStream, Future<void>? cancelFuture) async {
    final clave = '${options.method} ${options.path}';
    _fake.peticiones.add(clave);
    _fake.cuerpos.add(options.data);
    final respuesta = _fake.respuestas[clave];
    if (respuesta == null) {
      return ResponseBody.fromString('{"detail":"no simulado: $clave"}', 500, headers: _json);
    }
    if (respuesta is int) {
      return ResponseBody.fromString('{"detail":"error simulado","codigo":"X"}', respuesta, headers: _json);
    }
    return ResponseBody.fromString(jsonEncode(respuesta), 200, headers: _json);
  }

  static final _json = {
    Headers.contentTypeHeader: ['application/json'],
  };

  @override
  void close({bool force = false}) {}
}
