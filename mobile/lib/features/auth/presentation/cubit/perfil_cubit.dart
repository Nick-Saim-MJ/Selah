import '../../../../core/bloc/async_state.dart';
import '../../../../core/usecase/usecase.dart';
import '../../domain/entities/perfil.dart';
import '../../domain/usecases/auth_usecases.dart';

/// Mi cuenta: datos y decisión de compartir el reporte con mi pastor.
class PerfilCubit extends AsyncCubit<Perfil> {
  PerfilCubit({required this._obtener, required this._compartir});

  final ObtenerPerfil _obtener;
  final CompartirReporteConPastor _compartir;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));

  Future<String?> compartirReporte(bool compartir) => mutar(() => _compartir(compartir));
}
