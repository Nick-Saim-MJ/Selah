import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../domain/entities/sesion.dart';
import '../../domain/repositories/auth_repository.dart';
import '../../domain/usecases/auth_usecases.dart';

part 'auth_event.dart';
part 'auth_state.dart';

/// Bloc global de sesión. El router escucha su estado para decidir a qué pantalla va cada rol.
class AuthBloc extends Bloc<AuthEvent, AuthState> {
  AuthBloc({
    required this._iniciarSesion,
    required this._registrarCuenta,
    required this._repository,
  }) : super(const AuthDesconocido()) {
    on<AuthIniciado>(_alIniciar);
    on<AuthLoginSolicitado>(_alLogin);
    on<AuthRegistroSolicitado>(_alRegistro);
    on<AuthCierreSolicitado>(_alCerrar);
    on<AuthSesionRefrescada>(_alRefrescar);
  }

  final IniciarSesion _iniciarSesion;
  final RegistrarCuenta _registrarCuenta;
  final AuthRepository _repository;

  Future<void> _alIniciar(AuthIniciado event, Emitter<AuthState> emit) async {
    final sesion = await _repository.sesionGuardada();
    emit(sesion == null ? const AuthNoAutenticado() : AuthAutenticado(sesion));
  }

  Future<void> _alLogin(AuthLoginSolicitado event, Emitter<AuthState> emit) async {
    emit(const AuthCargando());
    final r = await _iniciarSesion(CredencialesParams(email: event.email, password: event.password));
    emit(r.fold((f) => AuthNoAutenticado(error: f.mensaje), AuthAutenticado.new));
  }

  Future<void> _alRegistro(AuthRegistroSolicitado event, Emitter<AuthState> emit) async {
    emit(const AuthCargando());
    final r = await _registrarCuenta(RegistroParams(
      email: event.email,
      password: event.password,
      nombres: event.nombres,
      apellidos: event.apellidos,
      iglesiaId: event.iglesiaId,
    ));
    emit(r.fold((f) => AuthNoAutenticado(error: f.mensaje), AuthAutenticado.new));
  }

  Future<void> _alCerrar(AuthCierreSolicitado event, Emitter<AuthState> emit) async {
    await _repository.cerrarSesion();
    emit(AuthNoAutenticado(error: event.motivo));
  }

  /// Tras terminar el asistente o cambiar la contraseña: relee la cuenta y deja que el router decida.
  Future<void> _alRefrescar(AuthSesionRefrescada event, Emitter<AuthState> emit) async {
    final actual = state;
    if (actual is! AuthAutenticado) return;
    final nueva = await _repository.refrescarSesion(actual.sesion);
    emit(nueva == null ? const AuthNoAutenticado(error: 'Tu sesión expiró') : AuthAutenticado(nueva));
  }
}
