import '../../../../core/bloc/async_state.dart';
import '../../../../core/usecase/usecase.dart';
import '../../domain/reflexion_domain.dart';

class ReflexionCubit extends AsyncCubit<TarjetaReflexion> {
  ReflexionCubit({required this._obtener, required this._responder});

  final ObtenerReflexion _obtener;
  final ResponderReflexion _responder;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));

  /// Guarda la respuesta y deja la tarjeta actualizada. Devuelve el error, o null si salió bien.
  Future<String?> responder(String texto) async {
    final r = await _responder(texto);
    return r.fold((f) => f.mensaje, (tarjeta) {
      emit(AsyncState(estado: Estado.exito, datos: tarjeta));
      return null;
    });
  }
}
