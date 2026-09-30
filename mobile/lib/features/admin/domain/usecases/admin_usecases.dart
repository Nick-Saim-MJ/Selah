import '../../../../core/error/failures.dart';
import '../../../../core/error/result.dart';
import '../../../../core/usecase/usecase.dart';
import '../entities/admin_entities.dart';
import '../repositories/admin_repository.dart';

class ObtenerResumenUsuarios implements UseCase<ResumenUsuarios, SinParametros> {
  const ObtenerResumenUsuarios(this._repo);

  final AdminRepository _repo;

  @override
  Future<Result<ResumenUsuarios>> call(SinParametros p) => _repo.resumen();
}

class BuscarUsuarios implements UseCase<PaginaUsuarios, FiltroUsuarios> {
  const BuscarUsuarios(this._repo);

  final AdminRepository _repo;

  @override
  Future<Result<PaginaUsuarios>> call(FiltroUsuarios f) => _repo.usuarios(f);
}

class CrearUsuario implements UseCase<UsuarioAdmin, NuevoUsuario> {
  const CrearUsuario(this._repo);

  final AdminRepository _repo;

  @override
  Future<Result<UsuarioAdmin>> call(NuevoUsuario u) {
    if (!u.email.contains('@')) {
      return Future.value(const Fallo(ValidacionFailure('Email inválido')));
    }
    if (u.nombres.trim().isEmpty) {
      return Future.value(const Fallo(ValidacionFailure('El nombre es obligatorio')));
    }
    if (u.passwordTemporal.length < 8) {
      return Future.value(const Fallo(ValidacionFailure('La contraseña temporal debe tener al menos 8 caracteres')));
    }
    if (u.rol.tieneFinanzas && u.iglesiaId == null) {
      return Future.value(const Fallo(ValidacionFailure('Elige la iglesia del pastor o hermano')));
    }
    return _repo.crearUsuario(u);
  }
}

class ActualizarUsuario implements UseCase<UsuarioAdmin, CambioUsuario> {
  const ActualizarUsuario(this._repo);

  final AdminRepository _repo;

  @override
  Future<Result<UsuarioAdmin>> call(CambioUsuario c) => _repo.actualizarUsuario(c);
}

/// `bloquear = true` bloquea la cuenta (deja de funcionar de inmediato); false la reactiva.
class CambiarEstadoUsuario implements UseCase<UsuarioAdmin, CambioEstadoParams> {
  const CambiarEstadoUsuario(this._repo);

  final AdminRepository _repo;

  @override
  Future<Result<UsuarioAdmin>> call(CambioEstadoParams p) => p.bloquear ? _repo.bloquear(p.id) : _repo.activar(p.id);
}

class ResetearPassword implements UseCase<void, ResetPasswordParams> {
  const ResetearPassword(this._repo);

  final AdminRepository _repo;

  @override
  Future<Result<void>> call(ResetPasswordParams p) {
    if (p.passwordTemporal.length < 8) {
      return Future.value(const Fallo(ValidacionFailure('La contraseña temporal debe tener al menos 8 caracteres')));
    }
    return _repo.resetearPassword(p.id, p.passwordTemporal);
  }
}

class ObtenerIntegraciones implements UseCase<VistaIntegraciones, SinParametros> {
  const ObtenerIntegraciones(this._repo);

  final AdminRepository _repo;

  @override
  Future<Result<VistaIntegraciones>> call(SinParametros p) => _repo.integraciones();
}

class CrearClienteApi implements UseCase<ClienteCreado, NuevoClienteApi> {
  const CrearClienteApi(this._repo);

  final AdminRepository _repo;

  @override
  Future<Result<ClienteCreado>> call(NuevoClienteApi c) {
    if (c.nombre.trim().isEmpty) {
      return Future.value(const Fallo(ValidacionFailure('El sistema necesita un nombre')));
    }
    if (c.scopes.isEmpty) {
      return Future.value(const Fallo(ValidacionFailure('Elige al menos un permiso')));
    }
    return _repo.crearCliente(c);
  }
}

class RevocarClienteApi implements UseCase<void, String> {
  const RevocarClienteApi(this._repo);

  final AdminRepository _repo;

  @override
  Future<Result<void>> call(String id) => _repo.revocarCliente(id);
}

class CrearWebhook implements UseCase<WebhookCreado, NuevoWebhook> {
  const CrearWebhook(this._repo);

  final AdminRepository _repo;

  @override
  Future<Result<WebhookCreado>> call(NuevoWebhook w) {
    final uri = Uri.tryParse(w.url.trim());
    if (uri == null || !(uri.isScheme('http') || uri.isScheme('https')) || uri.host.isEmpty) {
      return Future.value(const Fallo(ValidacionFailure('La URL debe empezar con http:// o https://')));
    }
    return _repo.crearWebhook(w);
  }
}

class EliminarWebhook implements UseCase<void, String> {
  const EliminarWebhook(this._repo);

  final AdminRepository _repo;

  @override
  Future<Result<void>> call(String id) => _repo.eliminarWebhook(id);
}

class CambioEstadoParams {
  const CambioEstadoParams({required this.id, required this.bloquear});

  final String id;
  final bool bloquear;
}

class ResetPasswordParams {
  const ResetPasswordParams({required this.id, required this.passwordTemporal});

  final String id;
  final String passwordTemporal;
}
