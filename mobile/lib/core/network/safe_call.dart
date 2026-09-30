import 'package:dio/dio.dart';

import '../error/failures.dart';
import '../error/result.dart';
import 'api_error_mapper.dart';

/// Ejecuta una llamada HTTP y convierte los errores de red/servidor en [Fallo].
/// Todos los repositorios de la capa data lo usan, incluidos los de APIs de otros equipos.
Future<Result<T>> intentar<T>(Future<T> Function() llamada) async {
  try {
    return Exito(await llamada());
  } on DioException catch (e) {
    return Fallo(mapearDioException(e));
  } on FormatException {
    return const Fallo(ServidorFailureRespuestaInvalida());
  }
}
