import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../features/admin/presentation/cubit/admin_cubits.dart';
import '../../features/admin/presentation/pages/admin_iglesias_page.dart';
import '../../features/admin/presentation/pages/admin_integraciones_page.dart';
import '../../features/admin/presentation/pages/admin_resumen_page.dart';
import '../../features/admin/presentation/pages/admin_shell.dart';
import '../../features/admin/presentation/pages/admin_usuarios_page.dart';
import '../../features/auth/domain/entities/perfil.dart';
import '../../features/auth/domain/usecases/auth_usecases.dart';
import '../../features/auth/presentation/bloc/auth_bloc.dart';
import '../../features/auth/presentation/cubit/perfil_cubit.dart';
import '../../features/auth/presentation/pages/cambiar_password_page.dart';
import '../../features/auth/presentation/pages/login_page.dart';
import '../../features/categorias/presentation/cubit/categorias_cubits.dart';
import '../../features/categorias/presentation/pages/categorias_page.dart';
import '../../features/configuracion/presentation/pages/asistente_page.dart';
import '../../features/configuracion/presentation/pages/configuracion_page.dart';
import '../../features/dashboard/presentation/cubit/inicio_cubit.dart';
import '../../features/dashboard/presentation/pages/dashboard_page.dart';
import '../../features/habitos/presentation/cubit/habitos_cubit.dart';
import '../../features/habitos/presentation/pages/habitos_page.dart';
import '../../features/iglesias/presentation/cubit/iglesias_cubit.dart';
import '../../features/mayordomia/presentation/cubit/mayordomia_cubits.dart';
import '../../features/mayordomia/presentation/pages/diezmos_page.dart';
import '../../features/metas/presentation/cubit/metas_cubits.dart';
import '../../features/metas/presentation/pages/metas_page.dart';
import '../../features/movimientos/domain/entities/tipo_movimiento.dart';
import '../../features/movimientos/presentation/bloc/movimientos_bloc.dart';
import '../../features/movimientos/presentation/bloc/registro_movimiento_bloc.dart';
import '../../features/movimientos/presentation/pages/movimientos_page.dart';
import '../../features/movimientos/presentation/pages/nuevo_movimiento_page.dart';
import '../../features/notificaciones/presentation/cubit/notificaciones_cubits.dart';
import '../../features/notificaciones/presentation/pages/notificaciones_page.dart';
import '../../features/onboarding/presentation/cubit/onboarding_cubit.dart';
import '../../features/onboarding/presentation/pages/onboarding_page.dart';
import '../../features/pastor/presentation/cubit/congregacion_cubit.dart';
import '../../features/pastor/presentation/pages/congregacion_page.dart';
import '../../features/reflexion/presentation/cubit/reflexion_cubit.dart';
import '../../features/reflexion/presentation/pages/reflexion_page.dart';
import '../../features/reportes/presentation/cubit/reportes_cubits.dart';
import '../../features/reportes/presentation/pages/reportes_page.dart';
import '../bloc/accion_cubit.dart';
import '../di/injection_container.dart';
import '../usecase/usecase.dart';
import '../widgets/recargar_al_cambiar.dart';
import 'app_shell.dart';
import 'redireccion.dart';
import 'rutas.dart';

final _raiz = GlobalKey<NavigatorState>();

/// Navegación declarativa por rol. Las redirecciones dependen solo del estado de los BLoCs globales
/// (ver [calcularRedireccion]): introducción → login → contraseña temporal → asistente → pantallas de su rol.
GoRouter crearRouter({required AuthBloc authBloc, required OnboardingCubit onboardingCubit}) {
  EstadoSesion sesionActual() => switch (authBloc.state) {
        AuthAutenticado(:final sesion) => SesionActiva(sesion),
        AuthNoAutenticado() || AuthCargando() => const SesionAusente(),
        AuthDesconocido() => const SesionDesconocida(),
      };

  return GoRouter(
    navigatorKey: _raiz,
    initialLocation: Rutas.cargando,
    refreshListenable: _RefrescoPorStreams([authBloc.stream, onboardingCubit.stream]),
    redirect: (context, state) =>
        calcularRedireccion(introVista: onboardingCubit.state, sesion: sesionActual(), ruta: state.matchedLocation),
    routes: [
      GoRoute(path: Rutas.cargando, builder: (_, _) => const _Cargando()),
      GoRoute(path: Rutas.onboarding, builder: (_, _) => const OnboardingPage()),
      GoRoute(
        path: Rutas.login,
        builder: (_, _) => BlocProvider(
          create: (_) => sl<IglesiasCubit>(param1: false)..cargar(),
          child: const LoginPage(),
        ),
      ),
      GoRoute(path: Rutas.cambiarPasswordObligatorio, builder: (_, _) => const CambiarPasswordPage(obligatorio: true)),
      GoRoute(path: Rutas.cambiarPassword, builder: (_, _) => const CambiarPasswordPage()),
      GoRoute(
        path: Rutas.asistente,
        builder: (_, _) => MultiBlocProvider(
          providers: [
            BlocProvider(create: (_) => sl<FuentesIngresoCubit>()..cargar()),
            BlocProvider(create: (_) => sl<ConfigMayordomiaCubit>()..cargar()),
            BlocProvider(create: (_) => sl<CategoriasCubit>()..cargar()),
            BlocProvider(create: (_) => AccionCubit<SinParametros, Perfil>(sl<CompletarConfiguracionInicial>())),
          ],
          child: const AsistentePage(),
        ),
      ),

      // ---------- Pantallas que se abren encima de las pestañas
      GoRoute(
        path: Rutas.configuracion,
        builder: (_, _) => MultiBlocProvider(
          providers: [
            BlocProvider(create: (_) => sl<PerfilCubit>()..cargar()),
            BlocProvider(create: (_) => sl<ConfigMayordomiaCubit>()..cargar()),
            BlocProvider(create: (_) => sl<PreferenciasNotificacionCubit>()..cargar()),
          ],
          child: const ConfiguracionPage(),
        ),
      ),
      GoRoute(
        path: Rutas.categorias,
        builder: (_, _) => BlocProvider(create: (_) => sl<CategoriasCubit>()..cargar(), child: const CategoriasPage()),
      ),
      GoRoute(
        path: Rutas.fuentesIngreso,
        builder: (_, _) => BlocProvider(create: (_) => sl<FuentesIngresoCubit>()..cargar(), child: const FuentesIngresoPage()),
      ),
      GoRoute(
        path: Rutas.notificaciones,
        builder: (_, _) => BlocProvider(create: (_) => sl<BandejaCubit>()..cargar(), child: const NotificacionesPage()),
      ),
      GoRoute(
        path: Rutas.reflexion,
        builder: (_, _) => BlocProvider(create: (_) => sl<ReflexionCubit>()..cargar(), child: const ReflexionPage()),
      ),
      GoRoute(
        path: Rutas.habitos,
        builder: (_, _) => BlocProvider(create: (_) => sl<HabitosCubit>()..cargar(), child: const HabitosPage()),
      ),
      GoRoute(
        path: Rutas.congregacion,
        builder: (_, _) => BlocProvider(create: (_) => sl<CongregacionCubit>()..cargar(), child: const CongregacionPage()),
      ),
      GoRoute(
        path: '${Rutas.movimientos}/nuevo/:tipo',
        builder: (_, state) => BlocProvider(
          create: (_) => sl<RegistroMovimientoBloc>(
            param1: TipoMovimiento.values.byName(state.pathParameters['tipo']!),
            param2: PreseleccionMovimiento(
              metaId: state.uri.queryParameters['metaId'],
              deudaId: state.uri.queryParameters['deudaId'],
            ),
          )..add(const RegistroIniciado()),
          child: const NuevoMovimientoPage(),
        ),
      ),

      // ---------- Hermano y pastor: 5 pestañas
      StatefulShellRoute.indexedStack(
        builder: (context, state, shell) => AppShell(shell: shell),
        branches: [
          StatefulShellBranch(routes: [
            GoRoute(
              path: Rutas.inicio,
              builder: (_, _) => BlocProvider(
                create: (_) => sl<InicioCubit>()..cargar(),
                child: RecargarAlCambiar.cubit<InicioCubit>(child: const DashboardPage()),
              ),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: Rutas.movimientos,
              builder: (_, _) => BlocProvider(
                create: (_) => sl<MovimientosBloc>()..add(const MovimientosSolicitados()),
                child: RecargarAlCambiar(
                  alCambiar: (c) => c.read<MovimientosBloc>().add(const MovimientosSolicitados()),
                  propio: (c) => c.read<MovimientosBloc>(),
                  child: const MovimientosPage(),
                ),
              ),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: Rutas.diezmos,
              builder: (_, _) => BlocProvider(
                create: (_) => sl<DiezmosCubit>()..cargar(),
                child: RecargarAlCambiar.cubit<DiezmosCubit>(child: const DiezmosPage()),
              ),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: Rutas.metas,
              builder: (_, _) => MultiBlocProvider(
                providers: [
                  BlocProvider(create: (_) => sl<MetasCubit>()..cargar()),
                  BlocProvider(create: (_) => sl<DeudasCubit>()..cargar()),
                ],
                child: RecargarAlCambiar.cubit<MetasCubit>(
                  child: RecargarAlCambiar.cubit<DeudasCubit>(child: const MetasPage()),
                ),
              ),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: Rutas.reportes,
              builder: (_, _) => MultiBlocProvider(
                providers: [
                  BlocProvider(create: (_) => sl<ReporteFinancieroCubit>()..cargar()),
                  BlocProvider(create: (_) => sl<ReporteMayordomiaCubit>()..cargar()),
                  BlocProvider(create: (_) => sl<PerfilCubit>()..cargar()),
                ],
                child: RecargarAlCambiar.cubit<ReporteFinancieroCubit>(
                  child: RecargarAlCambiar.cubit<ReporteMayordomiaCubit>(child: const ReportesPage()),
                ),
              ),
            ),
          ]),
        ],
      ),

      // ---------- Administrador: 4 pestañas propias, sin finanzas
      StatefulShellRoute.indexedStack(
        builder: (context, state, shell) => BlocProvider(
          // Una sola lista de iglesias compartida por la pestaña Iglesias y el formulario de usuarios
          create: (_) => sl<IglesiasCubit>(param1: true)..cargar(),
          child: AdminShell(shell: shell),
        ),
        branches: [
          StatefulShellBranch(routes: [
            GoRoute(
              path: Rutas.adminResumen,
              builder: (_, _) => BlocProvider(
                create: (_) => sl<AdminResumenCubit>()..cargar(),
                child: RecargarAlCambiar.cubit<AdminResumenCubit>(child: const AdminResumenPage()),
              ),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: Rutas.adminUsuarios,
              builder: (_, _) => BlocProvider(
                create: (_) => sl<UsuariosCubit>()..cargar(),
                child: const AdminUsuariosPage(),
              ),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(path: Rutas.adminIglesias, builder: (_, _) => const AdminIglesiasPage()),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: Rutas.adminIntegraciones,
              builder: (_, _) => BlocProvider(
                create: (_) => sl<IntegracionesCubit>()..cargar(),
                child: const AdminIntegracionesPage(),
              ),
            ),
          ]),
        ],
      ),
    ],
  );
}

class _Cargando extends StatelessWidget {
  const _Cargando();

  @override
  Widget build(BuildContext context) => const Scaffold(body: Center(child: CircularProgressIndicator()));
}

/// Notifica a go_router cuando cambia cualquiera de los estados observados.
class _RefrescoPorStreams extends ChangeNotifier {
  _RefrescoPorStreams(List<Stream<dynamic>> streams) {
    _subs = streams.map((s) => s.listen((_) => notifyListeners())).toList();
  }

  late final List<StreamSubscription<dynamic>> _subs;

  @override
  void dispose() {
    for (final s in _subs) {
      s.cancel();
    }
    super.dispose();
  }
}
