part of 'auth_bloc.dart';

sealed class AuthEvent extends Equatable {
  const AuthEvent();

  @override
  List<Object?> get props => [];
}

/// Al abrir la app: restaura la sesión guardada.
final class AuthIniciado extends AuthEvent {
  const AuthIniciado();
}

final class AuthLoginSolicitado extends AuthEvent {
  const AuthLoginSolicitado({required this.email, required this.password});

  final String email;
  final String password;

  @override
  List<Object?> get props => [email];
}

final class AuthRegistroSolicitado extends AuthEvent {
  const AuthRegistroSolicitado({
    required this.email,
    required this.password,
    required this.nombres,
    required this.iglesiaId,
    this.apellidos,
  });

  final String email;
  final String password;
  final String nombres;
  final String? apellidos;
  final String iglesiaId;

  @override
  List<Object?> get props => [email, nombres, apellidos, iglesiaId];
}

/// Volver a leer los datos de la cuenta (asistente terminado, contraseña cambiada).
final class AuthSesionRefrescada extends AuthEvent {
  const AuthSesionRefrescada();
}

/// Cierre manual o por token vencido (401).
final class AuthCierreSolicitado extends AuthEvent {
  const AuthCierreSolicitado({this.motivo});

  final String? motivo;

  @override
  List<Object?> get props => [motivo];
}
