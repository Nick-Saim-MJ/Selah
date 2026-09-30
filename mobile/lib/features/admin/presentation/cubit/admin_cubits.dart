import '../../../../core/bloc/async_state.dart';
import '../../../../core/usecase/usecase.dart';
import '../../domain/entities/admin_entities.dart';
import '../../domain/usecases/admin_usecases.dart';

class AdminResumenCubit extends AsyncCubit<ResumenUsuarios> {
  AdminResumenCubit(this._obtener);

  final ObtenerResumenUsuarios _obtener;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));
}

/// Listado de cuentas con filtros y "Ver más". Las acciones devuelven el mensaje de error (o null).
class UsuariosCubit extends AsyncCubit<PaginaUsuarios> {
  UsuariosCubit({
    required this._buscar,
    required this._crear,
    required this._actualizar,
    required this._cambiarEstado,
    required this._resetear,
  });

  final BuscarUsuarios _buscar;
  final CrearUsuario _crear;
  final ActualizarUsuario _actualizar;
  final CambiarEstadoUsuario _cambiarEstado;
  final ResetearPassword _resetear;

  FiltroUsuarios _filtro = const FiltroUsuarios();

  FiltroUsuarios get filtro => _filtro;

  @override
  Future<void> cargar() => ejecutar(() => _buscar(_filtro.copyWith(pagina: 0)));

  Future<void> filtrar(FiltroUsuarios nuevo) {
    _filtro = nuevo.copyWith(pagina: 0);
    return cargar();
  }

  /// Agrega la página siguiente al final de la lista.
  Future<void> verMas() async {
    final actual = state.datos;
    if (actual == null || !actual.hayMas || state.cargando) return;
    final r = await _buscar(_filtro.copyWith(pagina: actual.pagina + 1));
    if (isClosed) return;
    r.fold((_) {}, (p) => emit(AsyncState(
          estado: Estado.exito,
          datos: PaginaUsuarios(items: [...actual.items, ...p.items], pagina: p.pagina, totalPaginas: p.totalPaginas, total: p.total),
        )));
  }

  Future<String?> crear(NuevoUsuario u) => mutar(() => _crear(u));

  Future<String?> actualizar(CambioUsuario c) => mutar(() => _actualizar(c));

  Future<String?> cambiarEstado(String id, {required bool bloquear}) =>
      mutar(() => _cambiarEstado(CambioEstadoParams(id: id, bloquear: bloquear)));

  Future<String?> resetearPassword(String id, String temporal) =>
      mutar(() => _resetear(ResetPasswordParams(id: id, passwordTemporal: temporal)));
}

class IntegracionesCubit extends AsyncCubit<VistaIntegraciones> {
  IntegracionesCubit({
    required this._obtener,
    required this._crearCliente,
    required this._revocar,
    required this._crearWebhook,
    required this._eliminarWebhook,
  });

  final ObtenerIntegraciones _obtener;
  final CrearClienteApi _crearCliente;
  final RevocarClienteApi _revocar;
  final CrearWebhook _crearWebhook;
  final EliminarWebhook _eliminarWebhook;

  @override
  Future<void> cargar() => ejecutar(() => _obtener(const SinParametros()));

  /// Devuelve (error, creado). La key en claro solo existe en `creado`: hay que mostrarla en ese momento.
  Future<(String?, ClienteCreado?)> crearCliente(NuevoClienteApi c) async {
    final r = await _crearCliente(c);
    return r.fold((f) => (f.mensaje, null), (creado) {
      cargar();
      return (null, creado);
    });
  }

  Future<String?> revocar(String id) => mutar(() => _revocar(id));

  Future<(String?, WebhookCreado?)> crearWebhook(NuevoWebhook w) async {
    final r = await _crearWebhook(w);
    return r.fold((f) => (f.mensaje, null), (creado) {
      cargar();
      return (null, creado);
    });
  }

  Future<String?> eliminarWebhook(String id) => mutar(() => _eliminarWebhook(id));
}
