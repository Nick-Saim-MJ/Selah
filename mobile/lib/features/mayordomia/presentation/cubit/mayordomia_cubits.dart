import '../../../../core/bloc/async_state.dart';
import '../../../../core/usecase/usecase.dart';
import '../../domain/entities/mayordomia_entities.dart';
import '../../domain/usecases/mayordomia_usecases.dart';

/// Pantalla Diezmos: mes actual + historial. Al entregar, recarga.
class DiezmosCubit extends AsyncCubit<DiezmosVista> {
  DiezmosCubit({required this._obtener, required this._entregar});

  final ObtenerDiezmos _obtener;
  final EntregarMayordomia _entregar;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));

  /// Devuelve el mensaje de error, o null si salió bien.
  Future<String?> entregar(TipoApartado tipo) {
    final periodo = state.datos?.mesActual.periodo;
    if (periodo == null) return Future.value('Aún no se cargó el mes');
    return mutar(() => _entregar(EntregaParams(tipo: tipo, periodo: periodo)));
  }
}

/// Configuración de porcentajes de diezmo/ofrenda, día de entrega y Modo Sábado.
class ConfigMayordomiaCubit extends AsyncCubit<ConfigMayordomia> {
  ConfigMayordomiaCubit({required this._obtener, required this._guardar});

  final ObtenerConfigMayordomia _obtener;
  final GuardarConfigMayordomia _guardar;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));

  Future<String?> guardar(ConfigMayordomia config) => mutar(() => _guardar(config));
}
