import 'package:dio/dio.dart';

import '../../../core/domain/rol_usuario.dart';
import '../../../core/error/result.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/safe_call.dart';
import '../../../core/utils/json_utils.dart';
import '../domain/entities/admin_entities.dart';
import '../domain/repositories/admin_repository.dart';

UsuarioAdmin _usuarioDe(Map<String, dynamic> j) => UsuarioAdmin(
      id: j['id'] as String,
      email: j['email'] as String,
      nombres: j['nombres'] as String,
      apellidos: j['apellidos'] as String?,
      rol: RolUsuario.desdeApi(j['rol'] as String),
      bloqueado: j['estado'] == 'BLOQUEADO',
      iglesiaId: j['iglesiaId'] as String?,
      iglesiaNombre: j['iglesiaNombre'] as String?,
      debeCambiarPassword: j['debeCambiarPassword'] as bool? ?? false,
    );

ClienteApi _clienteDe(Map<String, dynamic> j) => ClienteApi(
      id: j['id'] as String,
      nombre: j['nombre'] as String,
      descripcion: j['descripcion'] as String?,
      responsableEmail: j['responsableEmail'] as String?,
      prefijoKey: j['prefijoKey'] as String,
      scopes: (j['scopes'] as List).cast<String>(),
      activo: j['activo'] as bool,
      expiraEn: fechaN(j['expiraEn']),
      ultimoUso: fechaN(j['ultimoUso']),
    );

Webhook _webhookDe(Map<String, dynamic> j) => Webhook(
      id: j['id'] as String,
      clienteApiId: j['clienteApiId'] as String,
      tipoEvento: j['tipoEvento'] as String,
      url: j['url'] as String,
      activa: j['activa'] as bool,
    );

class AdminRepositoryImpl implements AdminRepository {
  const AdminRepositoryImpl(this._dio);

  static const _tamanioPagina = 30;

  final Dio _dio;

  String get _base => ApiEndpoints.admin;

  @override
  Future<Result<ResumenUsuarios>> resumen() => intentar(() async {
        final j = (await _dio.get<Map<String, dynamic>>('$_base/usuarios/resumen')).data!;
        final porRol = <RolUsuario, int>{};
        (j['activosPorRol'] as Map<String, dynamic>).forEach((rol, n) => porRol[RolUsuario.desdeApi(rol)] = (n as num).toInt());
        return ResumenUsuarios(activosPorRol: porRol, bloqueados: (j['bloqueados'] as num).toInt(), total: (j['total'] as num).toInt());
      });

  @override
  Future<Result<PaginaUsuarios>> usuarios(FiltroUsuarios f) => intentar(() async {
        final r = await _dio.get<Map<String, dynamic>>('$_base/usuarios', queryParameters: {
          'rol': ?f.rol?.api,
          if (f.bloqueados) 'estado': 'BLOQUEADO',
          if (f.texto.trim().isNotEmpty) 'q': f.texto.trim(),
          'pagina': f.pagina,
          'tamanio': _tamanioPagina,
        });
        final j = r.data!;
        return PaginaUsuarios(
          items: lista(j['contenido']).map(_usuarioDe).toList(),
          pagina: (j['pagina'] as num).toInt(),
          totalPaginas: (j['totalPaginas'] as num).toInt(),
          total: (j['totalElementos'] as num).toInt(),
        );
      });

  @override
  Future<Result<UsuarioAdmin>> crearUsuario(NuevoUsuario u) => intentar(() async {
        final r = await _dio.post<Map<String, dynamic>>('$_base/usuarios', data: {
          'email': u.email.trim(),
          'nombres': u.nombres.trim(),
          'apellidos': u.apellidos?.trim().isEmpty ?? true ? null : u.apellidos!.trim(),
          'rol': u.rol.api,
          'iglesiaId': u.iglesiaId,
          'passwordTemporal': u.passwordTemporal,
        });
        return _usuarioDe(r.data!);
      });

  @override
  Future<Result<UsuarioAdmin>> actualizarUsuario(CambioUsuario c) => intentar(() async {
        final r = await _dio.put<Map<String, dynamic>>('$_base/usuarios/${c.id}', data: {
          'nombres': c.nombres,
          'apellidos': c.apellidos,
          'rol': c.rol?.api,
          'iglesiaId': c.iglesiaId,
        });
        return _usuarioDe(r.data!);
      });

  @override
  Future<Result<UsuarioAdmin>> bloquear(String id) =>
      intentar(() async => _usuarioDe((await _dio.post<Map<String, dynamic>>('$_base/usuarios/$id/bloquear')).data!));

  @override
  Future<Result<UsuarioAdmin>> activar(String id) =>
      intentar(() async => _usuarioDe((await _dio.post<Map<String, dynamic>>('$_base/usuarios/$id/activar')).data!));

  @override
  Future<Result<void>> resetearPassword(String id, String passwordTemporal) => intentar(
      () => _dio.post<void>('$_base/usuarios/$id/reset-password', data: {'passwordTemporal': passwordTemporal}));

  @override
  Future<Result<VistaIntegraciones>> integraciones() => intentar(() async {
        final respuestas = await Future.wait([
          _dio.get<List<dynamic>>('$_base/clientes-api'),
          _dio.get<List<dynamic>>('$_base/webhooks'),
        ]);
        return VistaIntegraciones(
          clientes: lista(respuestas[0].data).map(_clienteDe).toList(),
          webhooks: lista(respuestas[1].data).map(_webhookDe).toList(),
        );
      });

  @override
  Future<Result<ClienteCreado>> crearCliente(NuevoClienteApi c) => intentar(() async {
        final r = await _dio.post<Map<String, dynamic>>('$_base/clientes-api', data: {
          'nombre': c.nombre.trim(),
          'descripcion': c.descripcion,
          'responsableEmail': c.responsableEmail?.trim().isEmpty ?? true ? null : c.responsableEmail!.trim(),
          'scopes': c.scopes,
          'diasVigencia': c.diasVigencia,
        });
        final j = r.data!;
        return ClienteCreado(cliente: _clienteDe(j['cliente'] as Map<String, dynamic>), apiKey: j['apiKey'] as String);
      });

  @override
  Future<Result<void>> revocarCliente(String id) => intentar(() => _dio.delete<void>('$_base/clientes-api/$id'));

  @override
  Future<Result<WebhookCreado>> crearWebhook(NuevoWebhook w) => intentar(() async {
        final r = await _dio.post<Map<String, dynamic>>('$_base/webhooks',
            data: {'clienteApiId': w.clienteApiId, 'tipoEvento': w.tipoEvento, 'url': w.url.trim()});
        final j = r.data!;
        return WebhookCreado(webhook: _webhookDe(j['webhook'] as Map<String, dynamic>), secreto: j['secreto'] as String);
      });

  @override
  Future<Result<void>> eliminarWebhook(String id) => intentar(() => _dio.delete<void>('$_base/webhooks/$id'));
}
