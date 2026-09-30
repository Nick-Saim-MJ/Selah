import 'package:equatable/equatable.dart';

import '../../../../core/domain/rol_usuario.dart';

/// Mi cuenta (GET /api/v1/perfil).
class Perfil extends Equatable {
  const Perfil({
    required this.id,
    required this.email,
    required this.nombres,
    required this.rol,
    this.apellidos,
    this.iglesiaId,
    this.iglesiaNombre,
    this.debeCambiarPassword = false,
    this.onboardingCompletado = false,
    this.comparteReporte = false,
  });

  final String id;
  final String email;
  final String nombres;
  final String? apellidos;
  final RolUsuario rol;
  final String? iglesiaId;
  final String? iglesiaNombre;
  final bool debeCambiarPassword;
  final bool onboardingCompletado;

  /// El hermano comparte su reporte 4T con su pastor.
  final bool comparteReporte;

  String get nombreCompleto => apellidos == null ? nombres : '$nombres $apellidos';

  @override
  List<Object?> get props => [id, email, nombres, apellidos, rol, iglesiaId, iglesiaNombre, debeCambiarPassword,
        onboardingCompletado, comparteReporte];
}
