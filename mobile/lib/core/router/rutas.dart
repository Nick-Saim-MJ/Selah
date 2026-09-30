import '../../features/movimientos/domain/entities/tipo_movimiento.dart';

/// Rutas de la app.
///
/// * Hermano y pastor: 5 pestañas (Inicio, Movimientos, Diezmos, Metas, Reportes) + pantallas encima.
/// * Administrador: 4 pestañas propias (Resumen, Usuarios, Iglesias, Integraciones), sin finanzas.
abstract final class Rutas {
  /// Pantalla de espera mientras se revisa si hay una sesión guardada.
  static const cargando = '/cargando';
  static const onboarding = '/onboarding';
  static const login = '/login';
  static const cambiarPasswordObligatorio = '/cambiar-password';
  static const asistente = '/asistente';

  // Tab bar del hermano/pastor (spec 2: máximo 5 pestañas)
  static const inicio = '/inicio';
  static const movimientos = '/movimientos';
  static const diezmos = '/diezmos';
  static const metas = '/metas';
  static const reportes = '/reportes';

  // Pantallas que se abren encima de las pestañas
  static const configuracion = '/configuracion';
  static const cambiarPassword = '/configuracion/password';
  static const categorias = '/configuracion/categorias';
  static const fuentesIngreso = '/configuracion/ingresos';
  static const notificaciones = '/notificaciones';
  static const reflexion = '/reflexion';
  static const habitos = '/habitos';
  static const congregacion = '/congregacion';

  // Pestañas del administrador
  static const adminResumen = '/admin/resumen';
  static const adminUsuarios = '/admin/usuarios';
  static const adminIglesias = '/admin/iglesias';
  static const adminIntegraciones = '/admin/integraciones';

  /// Formulario de nuevo movimiento. Para aportes y pagos se puede preseleccionar la meta o la deuda.
  static String nuevoMovimiento(TipoMovimiento tipo, {String? metaId, String? deudaId}) {
    final consulta = <String, String>{'metaId': ?metaId, 'deudaId': ?deudaId};
    final base = '$movimientos/nuevo/${tipo.name}';
    return consulta.isEmpty ? base : Uri(path: base, queryParameters: consulta).toString();
  }

  /// Ruta de inicio según el rol de la sesión.
  static String inicioDe({required bool esAdmin}) => esAdmin ? adminResumen : inicio;
}
