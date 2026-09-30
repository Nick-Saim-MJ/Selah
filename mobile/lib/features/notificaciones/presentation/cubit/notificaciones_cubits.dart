import '../../../../core/bloc/async_state.dart';
import '../../../../core/usecase/usecase.dart';
import '../../domain/notificaciones_domain.dart';

class BandejaCubit extends AsyncCubit<Bandeja> {
  BandejaCubit({required this._obtener, required this._marcar, required this._marcarTodas});

  final ObtenerBandeja _obtener;
  final MarcarNotificacionLeida _marcar;
  final MarcarTodasLeidas _marcarTodas;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));

  /// Marca una como leída y actualiza la lista al instante (sin esperar al servidor).
  Future<void> marcarLeida(String id) async {
    final actual = state.datos;
    if (actual == null) return;
    final n = actual.items.where((i) => i.id == id).firstOrNull;
    if (n == null || n.leida) return;
    emit(AsyncState(
      estado: Estado.exito,
      datos: Bandeja(
        items: [for (final i in actual.items) i.id == id ? i.comoLeida() : i],
        noLeidas: actual.noLeidas > 0 ? actual.noLeidas - 1 : 0,
      ),
    ));
    await _marcar(id);
  }

  Future<void> marcarTodasLeidas() async {
    final actual = state.datos;
    if (actual == null) return;
    emit(AsyncState(
      estado: Estado.exito,
      datos: Bandeja(items: [for (final i in actual.items) i.comoLeida()], noLeidas: 0),
    ));
    await _marcarTodas(const SinParametros());
  }
}

class PreferenciasNotificacionCubit extends AsyncCubit<PreferenciasNotificacion> {
  PreferenciasNotificacionCubit({required this._obtener, required this._guardar});

  final ObtenerPreferenciasNotificacion _obtener;
  final GuardarPreferenciasNotificacion _guardar;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));

  Future<String?> guardar(PreferenciasNotificacion p) => mutar(() => _guardar(p));
}
