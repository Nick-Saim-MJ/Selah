import '../../../../core/error/result.dart';
import '../entities/admin_entities.dart';

abstract interface class AdminRepository {
  Future<Result<ResumenUsuarios>> resumen();

  Future<Result<PaginaUsuarios>> usuarios(FiltroUsuarios filtro);

  Future<Result<UsuarioAdmin>> crearUsuario(NuevoUsuario usuario);

  Future<Result<UsuarioAdmin>> actualizarUsuario(CambioUsuario cambio);

  Future<Result<UsuarioAdmin>> bloquear(String id);

  Future<Result<UsuarioAdmin>> activar(String id);

  Future<Result<void>> resetearPassword(String id, String passwordTemporal);

  Future<Result<VistaIntegraciones>> integraciones();

  Future<Result<ClienteCreado>> crearCliente(NuevoClienteApi cliente);

  Future<Result<void>> revocarCliente(String id);

  Future<Result<WebhookCreado>> crearWebhook(NuevoWebhook webhook);

  Future<Result<void>> eliminarWebhook(String id);
}
