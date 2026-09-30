import '../../../../core/bloc/async_state.dart';
import '../../../reportes/presentation/cubit/reportes_cubits.dart';
import '../../domain/pastor_domain.dart';

class CongregacionCubit extends AsyncCubit<VistaCongregacion> with NavegacionPorMes<VistaCongregacion> {
  CongregacionCubit(this._obtener);

  final ObtenerCongregacion _obtener;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(periodo));
}
