import '../../../../core/bloc/async_state.dart';
import '../../../../core/usecase/usecase.dart';
import '../../domain/entities/categorias_entities.dart';
import '../../domain/usecases/categorias_usecases.dart';

class CategoriasCubit extends AsyncCubit<List<CategoriaGasto>> {
  CategoriasCubit({
    required this._obtener,
    required this._guardar,
    required this._ocultar,
  });

  final ObtenerCategorias _obtener;
  final GuardarCategoria _guardar;
  final OcultarCategoria _ocultar;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));

  Future<String?> guardar(CategoriaGasto c) => mutar(() => _guardar(c));

  Future<String?> ocultar(String id) => mutar(() => _ocultar(id));
}

class FuentesIngresoCubit extends AsyncCubit<List<FuenteIngreso>> {
  FuentesIngresoCubit({
    required this._obtener,
    required this._guardar,
    required this._ocultar,
  });

  final ObtenerFuentesIngreso _obtener;
  final GuardarFuenteIngreso _guardar;
  final OcultarFuenteIngreso _ocultar;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));

  Future<String?> guardar(FuenteIngreso f) => mutar(() => _guardar(f));

  Future<String?> ocultar(String id) => mutar(() => _ocultar(id));
}
