import 'dart:convert';

import '../../../../core/domain/rol_usuario.dart';
import '../../domain/entities/perfil.dart';
import '../../domain/entities/sesion.dart';

class SesionModel extends Sesion {
  const SesionModel({
    required super.usuarioId,
    required super.rol,
    required this.accessToken,
    super.hogarId,
    super.iglesiaId,
    super.nombres,
    super.onboardingCompletado,
    super.debeCambiarPassword,
  });

  final String accessToken;

  /// Respuesta de POST /api/v1/auth/login y /registro. Los campos nulos no viajan (hogarId del admin).
  factory SesionModel.fromJson(Map<String, dynamic> json) => SesionModel(
        accessToken: json['accessToken'] as String,
        usuarioId: json['usuarioId'] as String,
        rol: RolUsuario.desdeApi(json['rol'] as String),
        hogarId: json['hogarId'] as String?,
        iglesiaId: json['iglesiaId'] as String?,
        nombres: json['nombres'] as String?,
        onboardingCompletado: json['onboardingCompletado'] as bool? ?? false,
        debeCambiarPassword: json['debeCambiarPassword'] as bool? ?? false,
      );

  /// Reconstruye lo básico de la sesión desde el JWT guardado; `null` si está vencido o es inválido.
  /// Los indicadores que el token no lleva (asistente, contraseña temporal) se completan con GET /perfil.
  static SesionModel? desdeToken(String token, {DateTime? ahora}) {
    try {
      final partes = token.split('.');
      if (partes.length != 3) return null;
      final payload = jsonDecode(utf8.decode(base64Url.decode(base64Url.normalize(partes[1])))) as Map<String, dynamic>;
      final expira = DateTime.fromMillisecondsSinceEpoch((payload['exp'] as num).toInt() * 1000);
      if (expira.isBefore(ahora ?? DateTime.now())) return null;
      return SesionModel(
        accessToken: token,
        usuarioId: payload['sub'] as String,
        rol: RolUsuario.desdeApi(payload['rol'] as String),
        hogarId: payload['hogar_id'] as String?,
        iglesiaId: payload['iglesia_id'] as String?,
      );
    } catch (_) {
      // Token corrupto, de una versión anterior (sin rol) o con claims inesperados: sin sesión.
      return null;
    }
  }
}

class PerfilModel extends Perfil {
  const PerfilModel({
    required super.id,
    required super.email,
    required super.nombres,
    required super.rol,
    super.apellidos,
    super.iglesiaId,
    super.iglesiaNombre,
    super.debeCambiarPassword,
    super.onboardingCompletado,
    super.comparteReporte,
  });

  factory PerfilModel.fromJson(Map<String, dynamic> json) => PerfilModel(
        id: json['id'] as String,
        email: json['email'] as String,
        nombres: json['nombres'] as String,
        apellidos: json['apellidos'] as String?,
        rol: RolUsuario.desdeApi(json['rol'] as String),
        iglesiaId: json['iglesiaId'] as String?,
        iglesiaNombre: json['iglesiaNombre'] as String?,
        debeCambiarPassword: json['debeCambiarPassword'] as bool? ?? false,
        onboardingCompletado: json['onboardingCompletado'] as bool? ?? false,
        comparteReporte: json['comparteReporte'] as bool? ?? false,
      );
}
