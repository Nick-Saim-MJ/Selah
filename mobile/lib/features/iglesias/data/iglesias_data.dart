import 'package:dio/dio.dart';

import '../../../core/error/result.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/safe_call.dart';
import '../../../core/utils/json_utils.dart';
import '../domain/entities/iglesia.dart';
import '../domain/repositories/iglesias_repository.dart';

Iglesia _iglesiaDe(Map<String, dynamic> j) => Iglesia(
      id: j['id'] as String,
      nombre: j['nombre'] as String,
      ciudad: j['ciudad'] as String?,
      distrito: j['distrito'] as String?,
      activa: j['activa'] as bool? ?? true,
    );

class IglesiasRepositoryImpl implements IglesiasRepository {
  const IglesiasRepositoryImpl(this._dio);

  final Dio _dio;

  @override
  Future<Result<List<Iglesia>>> activas() => intentar(() async {
        final r = await _dio.get<List<dynamic>>(ApiEndpoints.iglesias);
        return lista(r.data).map(_iglesiaDe).toList();
      });

  @override
  Future<Result<List<Iglesia>>> todas() => intentar(() async {
        final r = await _dio.get<List<dynamic>>('${ApiEndpoints.admin}/iglesias');
        return lista(r.data).map(_iglesiaDe).toList();
      });

  @override
  Future<Result<Iglesia>> crear({required String nombre, String? ciudad, String? distrito}) => intentar(() async {
        final r = await _dio.post<Map<String, dynamic>>('${ApiEndpoints.admin}/iglesias',
            data: {'nombre': nombre, 'ciudad': ciudad, 'distrito': distrito});
        return _iglesiaDe(r.data!);
      });

  @override
  Future<Result<Iglesia>> actualizar(Iglesia i) => intentar(() async {
        final r = await _dio.put<Map<String, dynamic>>('${ApiEndpoints.admin}/iglesias/${i.id}',
            data: {'nombre': i.nombre, 'ciudad': i.ciudad, 'distrito': i.distrito, 'activa': i.activa});
        return _iglesiaDe(r.data!);
      });
}
