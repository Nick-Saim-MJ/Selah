import 'package:equatable/equatable.dart';

/// Errores que ven las capas domain/presentation. Nunca se propagan DioException
/// ni excepciones de plataforma fuera de la capa data.
sealed class Failure extends Equatable {
  const Failure(this.mensaje);

  final String mensaje;

  @override
  List<Object?> get props => [mensaje];
}

/// Sin conexión o tiempo de espera agotado.
final class ConexionFailure extends Failure {
  const ConexionFailure([super.mensaje = 'Sin conexión. Revisa tu internet e inténtalo de nuevo.']);
}

/// Token ausente o vencido: hay que volver a iniciar sesión.
final class SesionExpiradaFailure extends Failure {
  const SesionExpiradaFailure([super.mensaje = 'Tu sesión expiró. Inicia sesión nuevamente.']);
}

/// Regla de negocio rechazada por el backend (HTTP 422, application/problem+json).
final class ReglaNegocioFailure extends Failure {
  const ReglaNegocioFailure(super.mensaje, {this.codigo});

  final String? codigo;

  @override
  List<Object?> get props => [mensaje, codigo];
}

final class ValidacionFailure extends Failure {
  const ValidacionFailure(super.mensaje, {this.campos = const {}});

  final Map<String, String> campos;

  @override
  List<Object?> get props => [mensaje, campos];
}

final class ServidorFailure extends Failure {
  const ServidorFailure([super.mensaje = 'Ocurrió un error inesperado. Inténtalo más tarde.']);
}

/// El backend respondió algo que la app no entiende (versión desalineada).
final class ServidorFailureRespuestaInvalida extends Failure {
  const ServidorFailureRespuestaInvalida([super.mensaje = 'La respuesta del servidor no es válida. Actualiza la app.']);
}

/// 403: la cuenta no tiene permiso para esta acción.
final class AccesoDenegadoFailure extends Failure {
  const AccesoDenegadoFailure([super.mensaje = 'No tienes permiso para hacer esto.']);
}

final class CacheFailure extends Failure {
  const CacheFailure([super.mensaje = 'No se pudo leer la información guardada en el dispositivo.']);
}
