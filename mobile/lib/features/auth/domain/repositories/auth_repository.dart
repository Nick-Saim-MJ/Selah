import '../../../../core/error/result.dart';
import '../entities/perfil.dart';
import '../entities/sesion.dart';

abstract interface class AuthRepository {
  Future<Result<Sesion>> iniciarSesion({required String email, required String password});

  Future<Result<Sesion>> registrar({
    required String email,
    required String password,
    required String nombres,
    required String iglesiaId,
    String? apellidos,
    String moneda = 'PEN',
  });

  /// Sesión guardada en el dispositivo, o `null` si no hay o el token venció.
  Future<Sesion?> sesionGuardada();

  /// Vuelve a leer del servidor los datos de la cuenta (asistente terminado, contraseña cambiada, rol…).
  Future<Sesion?> refrescarSesion(Sesion actual);

  Future<void> cerrarSesion();

  Future<Result<Perfil>> obtenerPerfil();

  Future<Result<Perfil>> compartirReporte(bool compartir);

  Future<Result<Perfil>> completarConfiguracionInicial();

  Future<Result<void>> cambiarPassword({required String actual, required String nueva});
}
