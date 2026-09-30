import 'package:dio/dio.dart';

import '../config/app_config.dart';

/// Fábrica de clientes HTTP. Hay un [Dio] por API: el del backend propio
/// (con JWT) y uno por cada API de otro equipo (ver [ExternalApiRegistry]).
abstract final class ApiClient {
  static Dio crear({required String baseUrl, List<Interceptor> interceptores = const []}) {
    final dio = Dio(
      BaseOptions(
        baseUrl: baseUrl,
        connectTimeout: AppConfig.connectTimeout,
        receiveTimeout: AppConfig.receiveTimeout,
        contentType: Headers.jsonContentType,
        responseType: ResponseType.json,
      ),
    );
    dio.interceptors.addAll(interceptores);
    return dio;
  }
}

/// Endpoints del backend SelahFinance (contrato: /swagger-ui.html).
abstract final class ApiEndpoints {
  static const registro = '/api/v1/auth/registro';
  static const login = '/api/v1/auth/login';
  static const cambiarPassword = '/api/v1/auth/cambiar-password';
  static const perfil = '/api/v1/perfil';
  static const iglesias = '/api/v1/iglesias';
  static const inicio = '/api/v1/inicio';
  static const categorias = '/api/v1/categorias';
  static const fuentesIngreso = '/api/v1/fuentes-ingreso';
  static const metas = '/api/v1/metas';
  static const deudas = '/api/v1/deudas';
  static const habitos = '/api/v1/habitos';
  static const reflexion = '/api/v1/reflexion/actual';
  static const notificaciones = '/api/v1/notificaciones';
  static const reportes = '/api/v1/reportes';
  static const historialMayordomia = '/api/v1/mayordomia/historial';
  static const pastor = '/api/v1/pastor';
  static const admin = '/api/v1/admin';
  static const movimientos = '/api/v1/movimientos';
  static const simulacionMayordomia = '/api/v1/mayordomia/simulacion';
  static const resumenMayordomia = '/api/v1/mayordomia/resumen';
  static const entregasMayordomia = '/api/v1/mayordomia/entregas';
  static const configuracionMayordomia = '/api/v1/hogar/configuracion-mayordomia';
}
