import '../../../../core/bloc/async_state.dart';
import '../../../../core/bloc/datos_cambiados.dart';
import '../../../../core/usecase/usecase.dart';
import '../../domain/entities/habitos_entities.dart';
import '../../domain/usecases/habitos_usecases.dart';

class HabitosCubit extends AsyncCubit<List<HabitoDelDia>> {
  HabitosCubit({required this._obtener, required this._registrar});

  final ObtenerHabitos _obtener;
  final RegistrarHabito _registrar;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));

  /// Guarda el valor de hoy y reemplaza ese hábito en la lista (sin recargar todo).
  /// Devuelve el mensaje de error, o null si salió bien.
  Future<String?> registrar(String codigo, double valor) async {
    final r = await _registrar(RegistroHabitoParams(codigo: codigo, valor: valor));
    return r.fold((f) => f.mensaje, (actualizado) {
      final lista = state.datos ?? const <HabitoDelDia>[];
      emit(AsyncState(
        estado: Estado.exito,
        datos: [for (final h in lista) h.codigo == codigo ? actualizado : h],
      ));
      datosCambiados.avisar(this); // el reporte 4T y Reportes se recalculan
      return null;
    });
  }
}
