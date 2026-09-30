import 'package:equatable/equatable.dart';

import '../../../../core/domain/rol_usuario.dart';

class UsuarioAdmin extends Equatable {
  const UsuarioAdmin({
    required this.id,
    required this.email,
    required this.nombres,
    required this.rol,
    required this.bloqueado,
    this.apellidos,
    this.iglesiaId,
    this.iglesiaNombre,
    this.debeCambiarPassword = false,
  });

  final String id;
  final String email;
  final String nombres;
  final String? apellidos;
  final RolUsuario rol;
  final bool bloqueado;
  final String? iglesiaId;
  final String? iglesiaNombre;
  final bool debeCambiarPassword;

  String get nombreCompleto => apellidos == null ? nombres : '$nombres $apellidos';

  @override
  List<Object?> get props =>
      [id, email, nombres, apellidos, rol, bloqueado, iglesiaId, iglesiaNombre, debeCambiarPassword];
}

/// Filtros del listado de cuentas.
class FiltroUsuarios extends Equatable {
  const FiltroUsuarios({this.rol, this.bloqueados = false, this.texto = '', this.pagina = 0});

  final RolUsuario? rol;

  /// Ver solo las cuentas bloqueadas.
  final bool bloqueados;
  final String texto;
  final int pagina;

  FiltroUsuarios copyWith({RolUsuario? rol, bool limpiarRol = false, bool? bloqueados, String? texto, int? pagina}) =>
      FiltroUsuarios(
        rol: limpiarRol ? null : (rol ?? this.rol),
        bloqueados: bloqueados ?? this.bloqueados,
        texto: texto ?? this.texto,
        pagina: pagina ?? this.pagina,
      );

  @override
  List<Object?> get props => [rol, bloqueados, texto, pagina];
}

class PaginaUsuarios extends Equatable {
  const PaginaUsuarios({required this.items, required this.pagina, required this.totalPaginas, required this.total});

  final List<UsuarioAdmin> items;
  final int pagina;
  final int totalPaginas;
  final int total;

  bool get hayMas => pagina + 1 < totalPaginas;

  @override
  List<Object?> get props => [items, pagina, totalPaginas, total];
}

class ResumenUsuarios extends Equatable {
  const ResumenUsuarios({required this.activosPorRol, required this.bloqueados, required this.total});

  final Map<RolUsuario, int> activosPorRol;
  final int bloqueados;
  final int total;

  @override
  List<Object?> get props => [activosPorRol, bloqueados, total];
}

/// Datos para crear una cuenta (pastor, hermano o admin) con contraseña temporal.
class NuevoUsuario extends Equatable {
  const NuevoUsuario({
    required this.email,
    required this.nombres,
    required this.rol,
    required this.passwordTemporal,
    this.apellidos,
    this.iglesiaId,
  });

  final String email;
  final String nombres;
  final String? apellidos;
  final RolUsuario rol;
  final String? iglesiaId;
  final String passwordTemporal;

  @override
  List<Object?> get props => [email, nombres, apellidos, rol, iglesiaId, passwordTemporal];
}

/// Cambios permitidos sobre una cuenta existente. El rol solo cambia entre pastor y hermano.
class CambioUsuario extends Equatable {
  const CambioUsuario({required this.id, this.nombres, this.apellidos, this.rol, this.iglesiaId});

  final String id;
  final String? nombres;
  final String? apellidos;
  final RolUsuario? rol;
  final String? iglesiaId;

  @override
  List<Object?> get props => [id, nombres, apellidos, rol, iglesiaId];
}

/// Sistema de otro equipo con acceso a la API por API key.
class ClienteApi extends Equatable {
  const ClienteApi({
    required this.id,
    required this.nombre,
    required this.prefijoKey,
    required this.scopes,
    required this.activo,
    this.descripcion,
    this.responsableEmail,
    this.expiraEn,
    this.ultimoUso,
  });

  final String id;
  final String nombre;
  final String? descripcion;
  final String? responsableEmail;

  /// Primeros caracteres de la key, para identificarla (la completa solo se muestra al crearla).
  final String prefijoKey;
  final List<String> scopes;
  final bool activo;
  final DateTime? expiraEn;
  final DateTime? ultimoUso;

  @override
  List<Object?> get props =>
      [id, nombre, descripcion, responsableEmail, prefijoKey, scopes, activo, expiraEn, ultimoUso];
}

class NuevoClienteApi extends Equatable {
  const NuevoClienteApi({required this.nombre, required this.scopes, this.descripcion, this.responsableEmail, this.diasVigencia});

  final String nombre;
  final String? descripcion;
  final String? responsableEmail;
  final List<String> scopes;
  final int? diasVigencia;

  @override
  List<Object?> get props => [nombre, descripcion, responsableEmail, scopes, diasVigencia];
}

/// Se devuelve UNA sola vez, al crear el cliente.
class ClienteCreado extends Equatable {
  const ClienteCreado({required this.cliente, required this.apiKey});

  final ClienteApi cliente;
  final String apiKey;

  @override
  List<Object?> get props => [cliente, apiKey];
}

class Webhook extends Equatable {
  const Webhook({required this.id, required this.clienteApiId, required this.tipoEvento, required this.url, required this.activa});

  final String id;
  final String clienteApiId;
  final String tipoEvento;
  final String url;
  final bool activa;

  @override
  List<Object?> get props => [id, clienteApiId, tipoEvento, url, activa];
}

class NuevoWebhook extends Equatable {
  const NuevoWebhook({required this.clienteApiId, required this.tipoEvento, required this.url});

  final String clienteApiId;
  final String tipoEvento;
  final String url;

  @override
  List<Object?> get props => [clienteApiId, tipoEvento, url];
}

/// El secreto de firma se devuelve UNA sola vez, al crear el webhook.
class WebhookCreado extends Equatable {
  const WebhookCreado({required this.webhook, required this.secreto});

  final Webhook webhook;
  final String secreto;

  @override
  List<Object?> get props => [webhook, secreto];
}

class VistaIntegraciones extends Equatable {
  const VistaIntegraciones({required this.clientes, required this.webhooks});

  final List<ClienteApi> clientes;
  final List<Webhook> webhooks;

  @override
  List<Object?> get props => [clientes, webhooks];
}
