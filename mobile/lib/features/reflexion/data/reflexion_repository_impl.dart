import 'package:dio/dio.dart';

import '../../../core/domain/dimension.dart';
import '../../../core/error/result.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/safe_call.dart';
import '../../../core/utils/json_utils.dart';
import '../domain/reflexion_domain.dart';

TarjetaReflexion _tarjetaDe(Map<String, dynamic> j) {
  final p = j['pregunta'] as Map<String, dynamic>;
  return TarjetaReflexion(
    visible: j['visible'] as bool,
    pregunta: PreguntaReflexion(
      id: (p['id'] as num).toInt(),
      texto: p['texto'] as String,
      referenciaBiblica: p['referenciaBiblica'] as String?,
      dimension: p['dimension'] == null ? null : Dimension.desdeApi(p['dimension'] as String),
    ),
    respuesta: j['respuesta'] as String?,
    entro: dbl(j['entro']),
    gasto: dbl(j['gasto']),
    diezmoAlDia: j['diezmoAlDia'] as bool?,
  );
}

class ReflexionRepositoryImpl implements ReflexionRepository {
  const ReflexionRepositoryImpl(this._dio);

  final Dio _dio;

  @override
  Future<Result<TarjetaReflexion>> actual() =>
      intentar(() async => _tarjetaDe((await _dio.get<Map<String, dynamic>>(ApiEndpoints.reflexion)).data!));

  @override
  Future<Result<TarjetaReflexion>> responder(String respuesta) => intentar(() async =>
      _tarjetaDe((await _dio.put<Map<String, dynamic>>(ApiEndpoints.reflexion, data: {'respuesta': respuesta})).data!));
}
