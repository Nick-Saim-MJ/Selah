import '../../../../core/bloc/async_state.dart';
import '../../../../core/utils/formato.dart';
import '../../domain/entities/reportes_entities.dart';
import '../../domain/usecases/reportes_usecases.dart';

/// Navegación por meses compartida por los dos reportes.
mixin NavegacionPorMes<T> on AsyncCubit<T> {
  late DateTime _mes = DateTime(DateTime.now().year, DateTime.now().month);

  String get periodo => Formato.periodo(_mes);

  bool get esMesActual {
    final hoy = DateTime.now();
    return _mes.year == hoy.year && _mes.month == hoy.month;
  }

  /// +1 / −1 mes. No se puede avanzar más allá del mes en curso.
  Future<void> moverMes(int delta) {
    final nuevo = DateTime(_mes.year, _mes.month + delta);
    final hoy = DateTime.now();
    if (nuevo.isAfter(DateTime(hoy.year, hoy.month))) return Future.value();
    _mes = nuevo;
    return cargar();
  }
}

class ReporteFinancieroCubit extends AsyncCubit<ReporteFinanciero> with NavegacionPorMes<ReporteFinanciero> {
  ReporteFinancieroCubit(this._obtener);

  final ObtenerReporteFinanciero _obtener;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(periodo));
}

class ReporteMayordomiaCubit extends AsyncCubit<ReporteMayordomia> with NavegacionPorMes<ReporteMayordomia> {
  ReporteMayordomiaCubit(this._obtener);

  final ObtenerReporteMayordomia _obtener;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(periodo));
}
