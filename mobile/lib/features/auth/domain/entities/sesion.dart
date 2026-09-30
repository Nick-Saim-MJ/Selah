import 'package:equatable/equatable.dart';

import '../../../../core/domain/rol_usuario.dart';

/// Sesión iniciada. `hogarId` es null para el administrador (no tiene finanzas).
class Sesion extends Equatable {
  const Sesion({
    required this.usuarioId,
    required this.rol,
    this.hogarId,
    this.iglesiaId,
    this.nombres,
    this.onboardingCompletado = false,
    this.debeCambiarPassword = false,
  });

  final String usuarioId;
  final RolUsuario rol;
  final String? hogarId;
  final String? iglesiaId;
  final String? nombres;

  /// Asistente de configuración inicial terminado.
  final bool onboardingCompletado;

  /// Cuenta creada por un admin o con contraseña reiniciada: debe cambiarla antes de seguir.
  final bool debeCambiarPassword;

  bool get esAdmin => rol == RolUsuario.admin;

  bool get esPastor => rol == RolUsuario.pastor;

  Sesion copyWith({String? nombres, bool? onboardingCompletado, bool? debeCambiarPassword}) => Sesion(
        usuarioId: usuarioId,
        rol: rol,
        hogarId: hogarId,
        iglesiaId: iglesiaId,
        nombres: nombres ?? this.nombres,
        onboardingCompletado: onboardingCompletado ?? this.onboardingCompletado,
        debeCambiarPassword: debeCambiarPassword ?? this.debeCambiarPassword,
      );

  @override
  List<Object?> get props =>
      [usuarioId, rol, hogarId, iglesiaId, nombres, onboardingCompletado, debeCambiarPassword];
}
