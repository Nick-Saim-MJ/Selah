import '../../../../core/bloc/async_state.dart';
import '../../../../core/usecase/usecase.dart';
import '../../domain/entities/iglesia.dart';
import '../../domain/usecases/iglesias_usecases.dart';

/// Lista de iglesias. En el registro solo las activas; en el panel del admin, todas y editables.
class IglesiasCubit extends AsyncCubit<List<Iglesia>> {
  IglesiasCubit({
    required this.esAdmin,
    required this._activas,
    required this._todas,
    required this._guardar,
  });

  final bool esAdmin;
  final ObtenerIglesiasActivas _activas;
  final ObtenerTodasLasIglesias _todas;
  final GuardarIglesia _guardar;

  @override
  Future<void> cargar() =>
      ejecutar(() => esAdmin ? _todas(const SinParametros()) : _activas(const SinParametros()));

  Future<String?> guardar(Iglesia iglesia) => mutar(() => _guardar(iglesia));
}
