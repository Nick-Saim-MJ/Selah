import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:get_it/get_it.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../features/admin/data/admin_repository_impl.dart';
import '../../features/admin/domain/repositories/admin_repository.dart';
import '../../features/admin/domain/usecases/admin_usecases.dart';
import '../../features/admin/presentation/cubit/admin_cubits.dart';
import '../../features/auth/data/datasources/auth_remote_data_source.dart';
import '../../features/auth/data/repositories/auth_repository_impl.dart';
import '../../features/auth/domain/repositories/auth_repository.dart';
import '../../features/auth/domain/usecases/auth_usecases.dart';
import '../../features/auth/presentation/bloc/auth_bloc.dart';
import '../../features/auth/presentation/cubit/perfil_cubit.dart';
import '../../features/categorias/data/categorias_repository_impl.dart';
import '../../features/categorias/domain/repositories/categorias_repository.dart';
import '../../features/categorias/domain/usecases/categorias_usecases.dart';
import '../../features/categorias/presentation/cubit/categorias_cubits.dart';
import '../../features/dashboard/data/inicio_repository_impl.dart';
import '../../features/dashboard/domain/dashboard_domain.dart';
import '../../features/dashboard/presentation/cubit/inicio_cubit.dart';
import '../../features/habitos/data/habitos_repository_impl.dart';
import '../../features/habitos/domain/repositories/habitos_repository.dart';
import '../../features/habitos/domain/usecases/habitos_usecases.dart';
import '../../features/habitos/presentation/cubit/habitos_cubit.dart';
import '../../features/iglesias/data/iglesias_data.dart';
import '../../features/iglesias/domain/repositories/iglesias_repository.dart';
import '../../features/iglesias/domain/usecases/iglesias_usecases.dart';
import '../../features/iglesias/presentation/cubit/iglesias_cubit.dart';
import '../../features/mayordomia/data/repositories/mayordomia_repository_impl.dart';
import '../../features/mayordomia/domain/repositories/mayordomia_repository.dart';
import '../../features/mayordomia/domain/usecases/mayordomia_usecases.dart';
import '../../features/mayordomia/domain/usecases/simular_desglose.dart';
import '../../features/mayordomia/presentation/cubit/mayordomia_cubits.dart';
import '../../features/metas/data/metas_repository_impl.dart';
import '../../features/metas/domain/repositories/metas_repository.dart';
import '../../features/metas/domain/usecases/metas_usecases.dart';
import '../../features/metas/presentation/cubit/metas_cubits.dart';
import '../../features/movimientos/data/datasources/movimientos_remote_data_source.dart';
import '../../features/movimientos/data/repositories/movimientos_repository_impl.dart';
import '../../features/movimientos/domain/entities/tipo_movimiento.dart';
import '../../features/movimientos/domain/repositories/movimientos_repository.dart';
import '../../features/movimientos/domain/usecases/eliminar_movimiento.dart';
import '../../features/movimientos/domain/usecases/obtener_movimientos.dart';
import '../../features/movimientos/domain/usecases/registrar_movimiento.dart';
import '../../features/movimientos/presentation/bloc/movimientos_bloc.dart';
import '../../features/movimientos/presentation/bloc/registro_movimiento_bloc.dart';
import '../../features/notificaciones/data/notificaciones_repository_impl.dart';
import '../../features/notificaciones/domain/notificaciones_domain.dart';
import '../../features/notificaciones/presentation/cubit/notificaciones_cubits.dart';
import '../../features/onboarding/data/repositories/onboarding_repository_impl.dart';
import '../../features/onboarding/domain/repositories/onboarding_repository.dart';
import '../../features/onboarding/presentation/cubit/onboarding_cubit.dart';
import '../../features/pastor/data/pastor_repository_impl.dart';
import '../../features/pastor/domain/pastor_domain.dart';
import '../../features/pastor/presentation/cubit/congregacion_cubit.dart';
import '../../features/reflexion/data/reflexion_repository_impl.dart';
import '../../features/reflexion/domain/reflexion_domain.dart';
import '../../features/reflexion/presentation/cubit/reflexion_cubit.dart';
import '../../features/reportes/data/reportes_repository_impl.dart';
import '../../features/reportes/domain/repositories/reportes_repository.dart';
import '../../features/reportes/domain/usecases/reportes_usecases.dart';
import '../../features/reportes/presentation/cubit/reportes_cubits.dart';
import '../config/app_config.dart';
import '../network/api_client.dart';
import '../network/auth_interceptor.dart';
import '../network/external_api_registry.dart';
import '../storage/token_storage.dart';

/// Service locator. Convención:
/// * `registerLazySingleton` para infraestructura, repositorios y casos de uso.
/// * `registerFactory` para BLoCs/Cubits de pantalla (uno nuevo cada vez que se abre).
/// * BLoCs globales (auth, onboarding) como singleton.
///
/// Los repositorios de features CRUD simples hablan con Dio directamente; `movimientos` y `auth` son la
/// referencia completa con data source separado.
final sl = GetIt.instance;

/// Nombre del Dio del backend propio (los de otros equipos viven en [ExternalApiRegistry]).
const _dioSelah = 'selah';

Dio get _dio => sl<Dio>(instanceName: _dioSelah);

Future<void> configurarDependencias() async {
  // ---------- Externos
  final prefs = await SharedPreferences.getInstance();
  sl
    ..registerLazySingleton(() => prefs)
    ..registerLazySingleton(() => const FlutterSecureStorage())
    ..registerLazySingleton(() => TokenStorage(sl()))
    ..registerLazySingleton<Dio>(
      () => ApiClient.crear(
        baseUrl: AppConfig.apiBaseUrl,
        interceptores: [
          AuthInterceptor(
            sl(),
            // Token vencido, cuenta bloqueada o rol cambiado: el backend responde 401 y se vuelve al login
            onSesionExpirada: () => sl<AuthBloc>().add(const AuthCierreSolicitado(motivo: 'Tu sesión expiró. Inicia sesión de nuevo.')),
          ),
        ],
      ),
      instanceName: _dioSelah,
    )
    ..registerLazySingleton(ExternalApiRegistry.new);

  _auth();
  _onboarding();
  _iglesias();
  _categorias();
  _mayordomia();
  _movimientos();
  _metas();
  _reportes();
  _habitos();
  _inicio();
  _reflexion();
  _notificaciones();
  _pastor();
  _admin();
}

void _auth() {
  sl
    ..registerLazySingleton<AuthRemoteDataSource>(() => AuthRemoteDataSourceImpl(_dio))
    ..registerLazySingleton<AuthRepository>(() => AuthRepositoryImpl(sl(), sl()))
    ..registerLazySingleton(() => IniciarSesion(sl()))
    ..registerLazySingleton(() => RegistrarCuenta(sl()))
    ..registerLazySingleton(() => ObtenerPerfil(sl()))
    ..registerLazySingleton(() => CompartirReporteConPastor(sl()))
    ..registerLazySingleton(() => CompletarConfiguracionInicial(sl()))
    ..registerLazySingleton(() => CambiarPassword(sl()))
    ..registerLazySingleton(() => AuthBloc(iniciarSesion: sl(), registrarCuenta: sl(), repository: sl()))
    ..registerFactory(() => PerfilCubit(obtener: sl(), compartir: sl()));
}

void _onboarding() {
  sl
    ..registerLazySingleton<OnboardingRepository>(() => OnboardingRepositoryImpl(sl()))
    ..registerLazySingleton(() => OnboardingCubit(sl()));
}

void _iglesias() {
  sl
    ..registerLazySingleton<IglesiasRepository>(() => IglesiasRepositoryImpl(_dio))
    ..registerLazySingleton(() => ObtenerIglesiasActivas(sl()))
    ..registerLazySingleton(() => ObtenerTodasLasIglesias(sl()))
    ..registerLazySingleton(() => GuardarIglesia(sl()))
    ..registerFactoryParam<IglesiasCubit, bool, void>(
      (esAdmin, _) => IglesiasCubit(esAdmin: esAdmin, activas: sl(), todas: sl(), guardar: sl()),
    );
}

void _categorias() {
  sl
    ..registerLazySingleton<CategoriasRepository>(() => CategoriasRepositoryImpl(_dio))
    ..registerLazySingleton(() => ObtenerCategorias(sl()))
    ..registerLazySingleton(() => GuardarCategoria(sl()))
    ..registerLazySingleton(() => OcultarCategoria(sl()))
    ..registerLazySingleton(() => ObtenerFuentesIngreso(sl()))
    ..registerLazySingleton(() => GuardarFuenteIngreso(sl()))
    ..registerLazySingleton(() => OcultarFuenteIngreso(sl()))
    ..registerFactory(() => CategoriasCubit(obtener: sl(), guardar: sl(), ocultar: sl()))
    ..registerFactory(() => FuentesIngresoCubit(obtener: sl(), guardar: sl(), ocultar: sl()));
}

void _mayordomia() {
  sl
    ..registerLazySingleton<MayordomiaRepository>(() => MayordomiaRepositoryImpl(_dio))
    ..registerLazySingleton(() => SimularDesglose(sl()))
    ..registerLazySingleton(() => ObtenerDiezmos(sl()))
    ..registerLazySingleton(() => EntregarMayordomia(sl()))
    ..registerLazySingleton(() => ObtenerConfigMayordomia(sl()))
    ..registerLazySingleton(() => GuardarConfigMayordomia(sl()))
    ..registerFactory(() => DiezmosCubit(obtener: sl(), entregar: sl()))
    ..registerFactory(() => ConfigMayordomiaCubit(obtener: sl(), guardar: sl()));
}

void _movimientos() {
  sl
    ..registerLazySingleton<MovimientosRemoteDataSource>(() => MovimientosRemoteDataSourceImpl(_dio))
    ..registerLazySingleton<MovimientosRepository>(() => MovimientosRepositoryImpl(sl()))
    ..registerLazySingleton(() => ObtenerMovimientos(sl()))
    ..registerLazySingleton(() => RegistrarMovimiento(sl()))
    ..registerLazySingleton(() => EliminarMovimiento(sl()))
    ..registerFactory(() => MovimientosBloc(sl(), sl()))
    ..registerFactoryParam<RegistroMovimientoBloc, TipoMovimiento, PreseleccionMovimiento>(
      (tipo, preseleccion) => RegistroMovimientoBloc(
        tipo: tipo,
        preseleccion: preseleccion,
        registrarMovimiento: sl(),
        simularDesglose: sl(),
        obtenerCategorias: sl(),
        obtenerMetas: sl(),
        obtenerDeudas: sl(),
        obtenerPresupuesto: sl(),
      ),
    );
}

void _metas() {
  sl
    ..registerLazySingleton<MetasRepository>(() => MetasRepositoryImpl(_dio))
    ..registerLazySingleton(() => ObtenerMetas(sl()))
    ..registerLazySingleton(() => GuardarMeta(sl()))
    ..registerLazySingleton(() => MarcarMetaPrincipal(sl()))
    ..registerLazySingleton(() => CancelarMeta(sl()))
    ..registerLazySingleton(() => ObtenerDeudas(sl()))
    ..registerLazySingleton(() => CrearDeuda(sl()))
    ..registerFactory(() => MetasCubit(obtener: sl(), guardar: sl(), principal: sl(), cancelar: sl()))
    ..registerFactory(() => DeudasCubit(obtener: sl(), crear: sl()));
}

void _reportes() {
  sl
    ..registerLazySingleton<ReportesRepository>(() => ReportesRepositoryImpl(_dio))
    ..registerLazySingleton(() => ObtenerReporteFinanciero(sl()))
    ..registerLazySingleton(() => ObtenerReporteMayordomia(sl()))
    ..registerFactory(() => ReporteFinancieroCubit(sl()))
    ..registerFactory(() => ReporteMayordomiaCubit(sl()));
}

void _habitos() {
  sl
    ..registerLazySingleton<HabitosRepository>(() => HabitosRepositoryImpl(_dio))
    ..registerLazySingleton(() => ObtenerHabitos(sl()))
    ..registerLazySingleton(() => RegistrarHabito(sl()))
    ..registerFactory(() => HabitosCubit(obtener: sl(), registrar: sl()));
}

void _inicio() {
  sl
    ..registerLazySingleton<InicioRepository>(() => InicioRepositoryImpl(_dio))
    ..registerLazySingleton(() => ObtenerInicio(sl()))
    ..registerFactory(() => InicioCubit(sl()));
}

void _reflexion() {
  sl
    ..registerLazySingleton<ReflexionRepository>(() => ReflexionRepositoryImpl(_dio))
    ..registerLazySingleton(() => ObtenerReflexion(sl()))
    ..registerLazySingleton(() => ResponderReflexion(sl()))
    ..registerFactory(() => ReflexionCubit(obtener: sl(), responder: sl()));
}

void _notificaciones() {
  sl
    ..registerLazySingleton<NotificacionesRepository>(() => NotificacionesRepositoryImpl(_dio))
    ..registerLazySingleton(() => ObtenerBandeja(sl()))
    ..registerLazySingleton(() => MarcarNotificacionLeida(sl()))
    ..registerLazySingleton(() => MarcarTodasLeidas(sl()))
    ..registerLazySingleton(() => ObtenerPreferenciasNotificacion(sl()))
    ..registerLazySingleton(() => GuardarPreferenciasNotificacion(sl()))
    ..registerFactory(() => BandejaCubit(obtener: sl(), marcar: sl(), marcarTodas: sl()))
    ..registerFactory(() => PreferenciasNotificacionCubit(obtener: sl(), guardar: sl()));
}

void _pastor() {
  sl
    ..registerLazySingleton<PastorRepository>(() => PastorRepositoryImpl(_dio))
    ..registerLazySingleton(() => ObtenerCongregacion(sl()))
    ..registerFactory(() => CongregacionCubit(sl()));
}

void _admin() {
  sl
    ..registerLazySingleton<AdminRepository>(() => AdminRepositoryImpl(_dio))
    ..registerLazySingleton(() => ObtenerResumenUsuarios(sl()))
    ..registerLazySingleton(() => BuscarUsuarios(sl()))
    ..registerLazySingleton(() => CrearUsuario(sl()))
    ..registerLazySingleton(() => ActualizarUsuario(sl()))
    ..registerLazySingleton(() => CambiarEstadoUsuario(sl()))
    ..registerLazySingleton(() => ResetearPassword(sl()))
    ..registerLazySingleton(() => ObtenerIntegraciones(sl()))
    ..registerLazySingleton(() => CrearClienteApi(sl()))
    ..registerLazySingleton(() => RevocarClienteApi(sl()))
    ..registerLazySingleton(() => CrearWebhook(sl()))
    ..registerLazySingleton(() => EliminarWebhook(sl()))
    ..registerFactory(() => AdminResumenCubit(sl()))
    ..registerFactory(() => UsuariosCubit(buscar: sl(), crear: sl(), actualizar: sl(), cambiarEstado: sl(), resetear: sl()))
    ..registerFactory(() => IntegracionesCubit(
          obtener: sl(),
          crearCliente: sl(),
          revocar: sl(),
          crearWebhook: sl(),
          eliminarWebhook: sl(),
        ));
}
