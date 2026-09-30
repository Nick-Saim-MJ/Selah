import 'package:dio/dio.dart';

import '../error/failures.dart';

/// Traduce errores HTTP (formato RFC 9457 del backend) a [Failure].
/// Lo usan todos los repositorios de la capa data, incluidos los que
/// consumen APIs de otros equipos.
Failure mapearDioException(DioException e) {
  switch (e.type) {
    case DioExceptionType.connectionTimeout:
    case DioExceptionType.sendTimeout:
    case DioExceptionType.receiveTimeout:
    case DioExceptionType.connectionError:
      return const ConexionFailure();
    default:
      break;
  }

  final status = e.response?.statusCode;
  final data = e.response?.data;
  final detalle = data is Map ? data['detail'] as String? : null;

  return switch (status) {
    401 => const SesionExpiradaFailure(),
    403 => AccesoDenegadoFailure(detalle ?? const AccesoDenegadoFailure().mensaje),
    404 => ServidorFailure(detalle ?? 'No se encontró lo que buscas.'),
    400 => ValidacionFailure(
        detalle ?? 'Datos inválidos',
        campos: data is Map && data['campos'] is Map
            ? Map<String, String>.from((data['campos'] as Map).map((k, v) => MapEntry('$k', '$v')))
            : const {},
      ),
    422 => ReglaNegocioFailure(detalle ?? 'Operación no permitida', codigo: data is Map ? data['codigo'] as String? : null),
    _ => ServidorFailure(detalle ?? const ServidorFailure().mensaje),
  };
}
