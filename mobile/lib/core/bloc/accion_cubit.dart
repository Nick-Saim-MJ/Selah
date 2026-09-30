import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../usecase/usecase.dart';

/// Estado de una acción de un solo disparo (guardar un formulario, crear algo, cambiar la contraseña…).
final class AccionState<R> extends Equatable {
  const AccionState({this.enviando = false, this.error, this.resultado});

  final bool enviando;
  final String? error;
  final R? resultado;

  @override
  List<Object?> get props => [enviando, error, resultado];
}

/// BLoC reutilizable para formularios: envuelve UN caso de uso. La pantalla llama `ejecutar` y
/// reacciona al estado (botón con progreso, mensaje de error, cerrar al terminar).
class AccionCubit<P, R> extends Cubit<AccionState<R>> {
  AccionCubit(this._casoDeUso) : super(const AccionState());

  final UseCase<R, P> _casoDeUso;

  /// Devuelve true si la acción terminó bien.
  Future<bool> ejecutar(P parametros) async {
    emit(const AccionState(enviando: true));
    final r = await _casoDeUso(parametros);
    if (isClosed) return false;
    return r.fold((f) {
      emit(AccionState(error: f.mensaje));
      return false;
    }, (valor) {
      emit(AccionState(resultado: valor));
      return true;
    });
  }
}
