import '../../../../core/error/failures.dart';
import '../../../../core/error/result.dart';
import '../../../../core/usecase/usecase.dart';
import '../entities/perfil.dart';
import '../entities/sesion.dart';
import '../repositories/auth_repository.dart';

class IniciarSesion implements UseCase<Sesion, CredencialesParams> {
  const IniciarSesion(this._repository);

  final AuthRepository _repository;

  @override
  Future<Result<Sesion>> call(CredencialesParams p) =>
      _repository.iniciarSesion(email: p.email.trim(), password: p.password);
}

class RegistrarCuenta implements UseCase<Sesion, RegistroParams> {
  const RegistrarCuenta(this._repository);

  final AuthRepository _repository;

  @override
  Future<Result<Sesion>> call(RegistroParams p) => _repository.registrar(
        email: p.email.trim(),
        password: p.password,
        nombres: p.nombres.trim(),
        apellidos: p.apellidos?.trim().isEmpty ?? true ? null : p.apellidos!.trim(),
        iglesiaId: p.iglesiaId,
      );
}

class ObtenerPerfil implements UseCase<Perfil, SinParametros> {
  const ObtenerPerfil(this._repository);

  final AuthRepository _repository;

  @override
  Future<Result<Perfil>> call(SinParametros p) => _repository.obtenerPerfil();
}

class CompartirReporteConPastor implements UseCase<Perfil, bool> {
  const CompartirReporteConPastor(this._repository);

  final AuthRepository _repository;

  @override
  Future<Result<Perfil>> call(bool compartir) => _repository.compartirReporte(compartir);
}

class CompletarConfiguracionInicial implements UseCase<Perfil, SinParametros> {
  const CompletarConfiguracionInicial(this._repository);

  final AuthRepository _repository;

  @override
  Future<Result<Perfil>> call(SinParametros p) => _repository.completarConfiguracionInicial();
}

class CambiarPassword implements UseCase<void, CambioPasswordParams> {
  const CambiarPassword(this._repository);

  final AuthRepository _repository;

  @override
  Future<Result<void>> call(CambioPasswordParams p) async {
    if (p.nueva.length < 8) {
      return const Fallo(ValidacionFailure('La contraseña nueva debe tener al menos 8 caracteres'));
    }
    if (p.nueva == p.actual) {
      return const Fallo(ValidacionFailure('La contraseña nueva debe ser distinta de la actual'));
    }
    return _repository.cambiarPassword(actual: p.actual, nueva: p.nueva);
  }
}

class CredencialesParams {
  const CredencialesParams({required this.email, required this.password});

  final String email;
  final String password;
}

class RegistroParams extends CredencialesParams {
  const RegistroParams({
    required super.email,
    required super.password,
    required this.nombres,
    required this.iglesiaId,
    this.apellidos,
  });

  final String nombres;
  final String? apellidos;
  final String iglesiaId;
}

class CambioPasswordParams {
  const CambioPasswordParams({required this.actual, required this.nueva});

  final String actual;
  final String nueva;
}
