import 'package:dio/dio.dart';

import '../../../core/domain/dimension.dart';
import '../../../core/error/result.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/safe_call.dart';
import '../../../core/utils/json_utils.dart';
import '../domain/entities/habitos_entities.dart';
import '../domain/repositories/habitos_repository.dart';

HabitoDelDia _habitoDe(Map<String, dynamic> j) => HabitoDelDia(
      codigo: j['codigo'] as String,
      dimension: Dimension.desdeApi(j['dimension'] as String),
      nombre: j['nombre'] as String,
      descripcion: j['descripcion'] as String?,
      unidad: UnidadHabito.desdeApi(j['unidad'] as String),
      diaria: j['frecuenciaMeta'] == 'DIARIA',
      metaValor: dbl(j['metaValor']),
      referenciaBiblica: j['referenciaBiblica'] as String?,
      valorHoy: dbl(j['valorHoy']),
      valorSemana: dbl(j['valorSemana']),
      cumplimientoPct: dbl(j['cumplimientoPct']),
    );

class HabitosRepositoryImpl implements HabitosRepository {
  const HabitosRepositoryImpl(this._dio);

  final Dio _dio;

  @override
  Future<Result<List<HabitoDelDia>>> delDia() => intentar(() async {
        final r = await _dio.get<List<dynamic>>(ApiEndpoints.habitos);
        return lista(r.data).map(_habitoDe).toList();
      });

  @override
  Future<Result<HabitoDelDia>> registrar(String codigo, double valor) => intentar(() async {
        final r = await _dio.post<Map<String, dynamic>>('${ApiEndpoints.habitos}/registros',
            data: {'codigo': codigo, 'valor': valor});
        return _habitoDe(r.data!);
      });
}
