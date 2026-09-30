import 'package:dio/dio.dart';

import '../../../../core/error/result.dart';
import '../../../../core/network/safe_call.dart';
import '../../../../core/storage/token_storage.dart';
import '../../domain/entities/perfil.dart';
import '../../domain/entities/sesion.dart';
import '../../domain/repositories/auth_repository.dart';
import '../datasources/auth_remote_data_source.dart';
import '../models/sesion_model.dart';

class AuthRepositoryImpl implements AuthRepository {
  const AuthRepositoryImpl(this._remote, this._tokens);

  final AuthRemoteDataSource _remote;
  final TokenStorage _tokens;

  @override
  Future<Result<Sesion>> iniciarSesion({required String email, required String password}) =>
      _guardando(() => _remote.login(email, password));

  @override
  Future<Result<Sesion>> registrar({
    required String email,
    required String password,
    required String nombres,
    required String iglesiaId,
    String? apellidos,
    String moneda = 'PEN',
  }) =>
      _guardando(() => _remote.registro({
            'email': email,
            'password': password,
            'nombres': nombres,
            'apellidos': ?apellidos,
            'iglesiaId': iglesiaId,
            'moneda': moneda,
          }));

  @override
  Future<Sesion?> sesionGuardada() async {
    final token = await _tokens.leer();
    if (token == null) return null;
    final basica = SesionModel.desdeToken(token);
    if (basica == null) {
      await _tokens.borrar();
      return null;
    }
    return refrescarSesion(basica);
  }

  @override
  Future<Sesion?> refrescarSesion(Sesion actual) async {
    try {
      final p = await _remote.perfil();
      return actual.copyWith(
        nombres: p.nombres,
        onboardingCompletado: p.onboardingCompletado,
        debeCambiarPassword: p.debeCambiarPassword,
      );
    } on DioException catch (e) {
      // Cuenta bloqueada o token que ya no vale: se cierra la sesión guardada.
      if (e.response?.statusCode == 401) {
        await _tokens.borrar();
        return null;
      }
      // Sin conexión u otro error: se conserva la sesión y cada pantalla mostrará su propio "Reintentar".
      return actual.copyWith(onboardingCompletado: true);
    }
  }

  @override
  Future<void> cerrarSesion() => _tokens.borrar();

  @override
  Future<Result<Perfil>> obtenerPerfil() => intentar(_remote.perfil);

  @override
  Future<Result<Perfil>> compartirReporte(bool compartir) => intentar(() => _remote.compartirReporte(compartir));

  @override
  Future<Result<Perfil>> completarConfiguracionInicial() => intentar(_remote.completarConfiguracionInicial);

  @override
  Future<Result<void>> cambiarPassword({required String actual, required String nueva}) =>
      intentar(() => _remote.cambiarPassword(actual, nueva));

  Future<Result<Sesion>> _guardando(Future<SesionModel> Function() llamada) async {
    final r = await intentar(llamada);
    if (r case Exito(:final valor)) {
      await _tokens.guardar(valor.accessToken);
    }
    return r;
  }
}
