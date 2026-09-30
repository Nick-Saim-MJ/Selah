part of 'auth_bloc.dart';

sealed class AuthState extends Equatable {
  const AuthState();

  @override
  List<Object?> get props => [];
}

/// Aún no se revisó si hay sesión guardada (splash).
final class AuthDesconocido extends AuthState {
  const AuthDesconocido();
}

final class AuthCargando extends AuthState {
  const AuthCargando();
}

final class AuthNoAutenticado extends AuthState {
  const AuthNoAutenticado({this.error});

  final String? error;

  @override
  List<Object?> get props => [error];
}

final class AuthAutenticado extends AuthState {
  const AuthAutenticado(this.sesion);

  final Sesion sesion;

  @override
  List<Object?> get props => [sesion];
}
