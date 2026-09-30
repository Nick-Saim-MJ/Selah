import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../error/result.dart';
import 'datos_cambiados.dart';

enum Estado { inicial, cargando, exito, fallo }

/// Estado genérico de "datos que se cargan": inicial → cargando → éxito | fallo.
/// Al recargar conserva los datos anteriores para no parpadear la pantalla.
final class AsyncState<T> extends Equatable {
  const AsyncState({this.estado = Estado.inicial, this.datos, this.error});

  final Estado estado;
  final T? datos;
  final String? error;

  bool get cargando => estado == Estado.cargando;
  bool get hayDatos => datos != null;

  @override
  List<Object?> get props => [estado, datos, error];
}

/// Base de los Cubits de pantallas de consulta/edición simple. Sigue siendo BLoC (flutter_bloc):
/// la UI solo llama métodos y pinta estados; la lógica vive en los casos de uso.
abstract class AsyncCubit<T> extends Cubit<AsyncState<T>> {
  AsyncCubit() : super(const AsyncState());

  /// Vuelve a cargar los datos de la pantalla.
  Future<void> cargar();

  Future<void> ejecutar(Future<Result<T>> Function() consulta) async {
    emit(AsyncState(estado: Estado.cargando, datos: state.datos));
    final r = await consulta();
    if (isClosed) return;
    emit(r.fold(
      (f) => AsyncState(estado: Estado.fallo, datos: state.datos, error: f.mensaje),
      (d) => AsyncState(estado: Estado.exito, datos: d),
    ));
  }

  /// Ejecuta una modificación; si sale bien recarga. Devuelve el mensaje de error (o null si todo salió bien)
  /// para que la pantalla lo muestre en un SnackBar.
  Future<String?> mutar<R>(Future<Result<R>> Function() accion) async {
    final r = await accion();
    return r.fold((f) => f.mensaje, (_) {
      cargar();
      datosCambiados.avisar(this); // las demás pantallas con datos derivados se recargan
      return null;
    });
  }
}
