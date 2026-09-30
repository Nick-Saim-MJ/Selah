import '../error/result.dart';

/// Contrato de todo caso de uso de la capa domain.
abstract interface class UseCase<T, P> {
  Future<Result<T>> call(P params);
}

/// Para casos de uso que no reciben parámetros.
final class SinParametros {
  const SinParametros();
}
