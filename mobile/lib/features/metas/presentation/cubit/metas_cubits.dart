import '../../../../core/bloc/async_state.dart';
import '../../../../core/usecase/usecase.dart';
import '../../domain/entities/metas_entities.dart';
import '../../domain/usecases/metas_usecases.dart';

class MetasCubit extends AsyncCubit<List<MetaAhorro>> {
  MetasCubit({
    required this._obtener,
    required this._guardar,
    required this._principal,
    required this._cancelar,
  });

  final ObtenerMetas _obtener;
  final GuardarMeta _guardar;
  final MarcarMetaPrincipal _principal;
  final CancelarMeta _cancelar;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));

  Future<String?> guardar(MetaAhorro meta) => mutar(() => _guardar(meta));

  Future<String?> hacerPrincipal(String id) => mutar(() => _principal(id));

  Future<String?> cancelar(String id) => mutar(() => _cancelar(id));
}

class DeudasCubit extends AsyncCubit<ResumenDeudas> {
  DeudasCubit({required this._obtener, required this._crear});

  final ObtenerDeudas _obtener;
  final CrearDeuda _crear;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));

  Future<String?> crear(NuevaDeuda deuda) => mutar(() => _crear(deuda));
}
