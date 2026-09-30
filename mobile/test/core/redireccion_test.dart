import 'package:flutter_test/flutter_test.dart';
import 'package:selah_finance/core/domain/rol_usuario.dart';
import 'package:selah_finance/core/router/redireccion.dart';
import 'package:selah_finance/core/router/rutas.dart';
import 'package:selah_finance/features/auth/domain/entities/sesion.dart';

Sesion _sesion(RolUsuario rol, {bool asistente = true, bool temporal = false}) => Sesion(
      usuarioId: 'u1',
      rol: rol,
      hogarId: rol.tieneFinanzas ? 'h1' : null,
      onboardingCompletado: asistente,
      debeCambiarPassword: temporal,
    );

String? _ir(String ruta, {EstadoSesion sesion = const SesionAusente(), bool intro = true}) =>
    calcularRedireccion(introVista: intro, sesion: sesion, ruta: ruta);

void main() {
  group('primer uso y sin sesión', () {
    test('sin haber visto la introducción todo lleva a la introducción', () {
      expect(_ir(Rutas.login, intro: false), Rutas.onboarding);
      expect(_ir(Rutas.onboarding, intro: false), isNull);
    });

    test('mientras se revisa la sesión guardada se espera en /cargando', () {
      expect(_ir(Rutas.inicio, sesion: const SesionDesconocida()), Rutas.cargando);
      expect(_ir(Rutas.cargando, sesion: const SesionDesconocida()), isNull);
    });

    test('sin sesión solo se puede estar en el login', () {
      expect(_ir(Rutas.inicio), Rutas.login);
      expect(_ir(Rutas.adminUsuarios), Rutas.login);
      expect(_ir(Rutas.login), isNull);
    });
  });

  group('con sesión', () {
    test('contraseña temporal: hay que cambiarla antes de cualquier otra pantalla', () {
      final s = SesionActiva(_sesion(RolUsuario.pastor, temporal: true));
      expect(_ir(Rutas.inicio, sesion: s), Rutas.cambiarPasswordObligatorio);
      expect(_ir(Rutas.cambiarPasswordObligatorio, sesion: s), isNull);
    });

    test('hermano y pastor sin asistente terminado van al asistente', () {
      for (final rol in [RolUsuario.hermano, RolUsuario.pastor]) {
        final s = SesionActiva(_sesion(rol, asistente: false));
        expect(_ir(Rutas.inicio, sesion: s), Rutas.asistente);
        expect(_ir(Rutas.asistente, sesion: s), isNull);
      }
    });

    test('el admin NO pasa por el asistente (no tiene finanzas)', () {
      final s = SesionActiva(_sesion(RolUsuario.admin, asistente: false));
      expect(_ir(Rutas.adminResumen, sesion: s), isNull);
    });

    test('al terminar de entrar cada rol aterriza en su pantalla', () {
      expect(_ir(Rutas.login, sesion: SesionActiva(_sesion(RolUsuario.hermano))), Rutas.inicio);
      expect(_ir(Rutas.cargando, sesion: SesionActiva(_sesion(RolUsuario.pastor))), Rutas.inicio);
      expect(_ir(Rutas.login, sesion: SesionActiva(_sesion(RolUsuario.admin))), Rutas.adminResumen);
      expect(_ir(Rutas.asistente, sesion: SesionActiva(_sesion(RolUsuario.hermano))), Rutas.inicio);
    });
  });

  group('cada rol solo entra a lo suyo', () {
    final admin = SesionActiva(_sesion(RolUsuario.admin));
    final pastor = SesionActiva(_sesion(RolUsuario.pastor));
    final hermano = SesionActiva(_sesion(RolUsuario.hermano));

    test('el admin no ve finanzas ni reportes ni la congregación', () {
      for (final ruta in [Rutas.inicio, Rutas.movimientos, Rutas.diezmos, Rutas.metas, Rutas.reportes, Rutas.congregacion, Rutas.habitos]) {
        expect(_ir(ruta, sesion: admin), Rutas.adminResumen, reason: ruta);
      }
    });

    test('el admin sí puede cambiar su contraseña y usar su panel', () {
      expect(_ir(Rutas.cambiarPassword, sesion: admin), isNull);
      for (final ruta in [Rutas.adminResumen, Rutas.adminUsuarios, Rutas.adminIglesias, Rutas.adminIntegraciones]) {
        expect(_ir(ruta, sesion: admin), isNull, reason: ruta);
      }
    });

    test('pastor y hermano no entran al panel del admin', () {
      for (final s in [pastor, hermano]) {
        expect(_ir(Rutas.adminUsuarios, sesion: s), Rutas.inicio);
        expect(_ir(Rutas.adminIntegraciones, sesion: s), Rutas.inicio);
      }
    });

    test('solo el pastor ve su congregación', () {
      expect(_ir(Rutas.congregacion, sesion: pastor), isNull);
      expect(_ir(Rutas.congregacion, sesion: hermano), Rutas.inicio);
    });

    test('hermano y pastor usan sus pantallas normales', () {
      for (final s in [pastor, hermano]) {
        for (final ruta in [Rutas.inicio, Rutas.movimientos, Rutas.reportes, Rutas.configuracion, Rutas.habitos]) {
          expect(_ir(ruta, sesion: s), isNull, reason: ruta);
        }
      }
    });
  });
}
