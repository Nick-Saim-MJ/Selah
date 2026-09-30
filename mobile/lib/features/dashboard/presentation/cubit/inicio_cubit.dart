import '../../../../core/bloc/async_state.dart';
import '../../../../core/usecase/usecase.dart';
import '../../domain/dashboard_domain.dart';
import '../../domain/entities/inicio.dart';

class InicioCubit extends AsyncCubit<Inicio> {
  InicioCubit(this._obtener);

  final ObtenerInicio _obtener;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));
}
