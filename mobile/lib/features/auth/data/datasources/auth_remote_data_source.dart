import 'package:dio/dio.dart';

import '../../../../core/network/api_client.dart';
import '../models/sesion_model.dart';

abstract interface class AuthRemoteDataSource {
  Future<SesionModel> login(String email, String password);

  Future<SesionModel> registro(Map<String, dynamic> datos);

  Future<PerfilModel> perfil();

  Future<PerfilModel> compartirReporte(bool compartir);

  Future<PerfilModel> completarConfiguracionInicial();

  Future<void> cambiarPassword(String actual, String nueva);
}

class AuthRemoteDataSourceImpl implements AuthRemoteDataSource {
  const AuthRemoteDataSourceImpl(this._dio);

  final Dio _dio;

  @override
  Future<SesionModel> login(String email, String password) async {
    final r = await _dio.post<Map<String, dynamic>>(ApiEndpoints.login, data: {'email': email, 'password': password});
    return SesionModel.fromJson(r.data!);
  }

  @override
  Future<SesionModel> registro(Map<String, dynamic> datos) async {
    final r = await _dio.post<Map<String, dynamic>>(ApiEndpoints.registro, data: datos);
    return SesionModel.fromJson(r.data!);
  }

  @override
  Future<PerfilModel> perfil() async {
    final r = await _dio.get<Map<String, dynamic>>(ApiEndpoints.perfil);
    return PerfilModel.fromJson(r.data!);
  }

  @override
  Future<PerfilModel> compartirReporte(bool compartir) async {
    final r = await _dio.put<Map<String, dynamic>>('${ApiEndpoints.perfil}/compartir-reporte', data: {'compartir': compartir});
    return PerfilModel.fromJson(r.data!);
  }

  @override
  Future<PerfilModel> completarConfiguracionInicial() async {
    final r = await _dio.post<Map<String, dynamic>>('${ApiEndpoints.perfil}/configuracion-inicial-completada');
    return PerfilModel.fromJson(r.data!);
  }

  @override
  Future<void> cambiarPassword(String actual, String nueva) =>
      _dio.post<void>(ApiEndpoints.cambiarPassword, data: {'passwordActual': actual, 'passwordNueva': nueva});
}
