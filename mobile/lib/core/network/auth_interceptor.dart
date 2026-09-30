import 'package:dio/dio.dart';

import '../storage/token_storage.dart';

/// Adjunta el JWT a cada request y notifica cuando el backend responde 401.
class AuthInterceptor extends Interceptor {
  AuthInterceptor(this._tokens, {this.onSesionExpirada});

  final TokenStorage _tokens;
  final void Function()? onSesionExpirada;

  @override
  Future<void> onRequest(RequestOptions options, RequestInterceptorHandler handler) async {
    final token = await _tokens.leer();
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    if (err.response?.statusCode == 401) {
      onSesionExpirada?.call();
    }
    handler.next(err);
  }
}
