import 'failures.dart';

/// Resultado de un caso de uso: [Exito] o [Fallo]. Obliga a manejar el error
/// con `switch` exhaustivo (Dart 3), sin try/catch en la presentación.
sealed class Result<T> {
  const Result();

  R fold<R>(R Function(Failure failure) siFallo, R Function(T valor) siExito) => switch (this) {
        Exito<T>(:final valor) => siExito(valor),
        Fallo<T>(:final failure) => siFallo(failure),
      };
}

final class Exito<T> extends Result<T> {
  const Exito(this.valor);

  final T valor;
}

final class Fallo<T> extends Result<T> {
  const Fallo(this.failure);

  final Failure failure;
}
