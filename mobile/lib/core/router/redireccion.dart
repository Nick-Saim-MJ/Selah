import '../../features/auth/domain/entities/sesion.dart';
import 'rutas.dart';

/// Estado de sesión que necesita el router (independiente de flutter_bloc para poder probarlo).
sealed class EstadoSesion {
  const EstadoSesion();
}

/// Aún no se revisó si hay una sesión guardada.
final class SesionDesconocida extends EstadoSesion {
  const SesionDesconocida();
}

final class SesionAusente extends EstadoSesion {
  const SesionAusente();
}

final class SesionActiva extends EstadoSesion {
  const SesionActiva(this.sesion);

  final Sesion sesion;
}

/// Decide a qué pantalla debe ir la app. Devuelve null si la ruta actual es correcta.
///
/// Orden de prioridad:
/// 1. Primer uso: introducción.
/// 2. Sin sesión: login.
/// 3. Contraseña temporal: cambiarla antes de seguir.
/// 4. Hermano/pastor sin asistente inicial terminado: asistente.
/// 5. Cada rol solo entra a lo suyo: el admin no ve finanzas; solo el pastor ve su congregación.
String? calcularRedireccion({required bool introVista, required EstadoSesion sesion, required String ruta}) {
  if (!introVista) {
    return ruta == Rutas.onboarding ? null : Rutas.onboarding;
  }
  switch (sesion) {
    case SesionDesconocida():
      return ruta == Rutas.cargando ? null : Rutas.cargando;
    case SesionAusente():
      return ruta == Rutas.login ? null : Rutas.login;
    case SesionActiva(:final sesion):
      if (sesion.debeCambiarPassword) {
        return ruta == Rutas.cambiarPasswordObligatorio ? null : Rutas.cambiarPasswordObligatorio;
      }
      if (!sesion.esAdmin && !sesion.onboardingCompletado) {
        return ruta == Rutas.asistente ? null : Rutas.asistente;
      }
      final inicio = Rutas.inicioDe(esAdmin: sesion.esAdmin);
      const pantallasDeEntrada = {
        Rutas.login,
        Rutas.onboarding,
        Rutas.cargando,
        Rutas.cambiarPasswordObligatorio,
        Rutas.asistente,
      };
      if (pantallasDeEntrada.contains(ruta)) return inicio;

      final esRutaAdmin = ruta.startsWith('/admin');
      if (sesion.esAdmin) {
        // El administrador solo gestiona cuentas: su panel y el cambio de contraseña
        return esRutaAdmin || ruta == Rutas.cambiarPassword ? null : inicio;
      }
      if (esRutaAdmin) return inicio;
      if (ruta == Rutas.congregacion && !sesion.esPastor) return inicio;
      return null;
  }
}
