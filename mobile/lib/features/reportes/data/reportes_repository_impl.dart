import 'package:dio/dio.dart';

import '../../../core/domain/dimension.dart';
import '../../../core/domain/semaforo.dart';
import '../../../core/error/result.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/safe_call.dart';
import '../../../core/utils/json_utils.dart';
import '../domain/entities/reportes_entities.dart';
import '../domain/repositories/reportes_repository.dart';

LineaPresupuesto _lineaDe(Map<String, dynamic> j) => LineaPresupuesto(
      categoriaId: j['categoriaId'] as String,
      nombre: j['nombre'] as String,
      color: j['color'] as String?,
      planeado: dbl(j['planeado']),
      real: dbl(j['real']),
      variacionPct: dblN(j['variacionPct']),
    );

class ReportesRepositoryImpl implements ReportesRepository {
  const ReportesRepositoryImpl(this._dio);

  final Dio _dio;

  @override
  Future<Result<ReporteFinanciero>> financiero(String? periodo) => intentar(() async {
        final r = await _dio.get<Map<String, dynamic>>('${ApiEndpoints.reportes}/financiero',
            queryParameters: {'periodo': ?periodo});
        final j = r.data!;
        return ReporteFinanciero(
          periodo: j['periodo'] as String,
          ingresos: dbl(j['ingresos']),
          gastos: dbl(j['gastos']),
          pagosDeuda: dbl(j['pagosDeuda']),
          aportesMeta: dbl(j['aportesMeta']),
          saldoDisponible: dbl(j['saldoDisponible']),
          apartadoTotal: dbl(j['apartadoTotal']),
          ratioGasto: dbl(j['ratioGasto']),
          ratioDeuda: dbl(j['ratioDeuda']),
          ratioAhorro: dbl(j['ratioAhorro']),
          semaforo: Semaforo.desdeApi(j['semaforo'] as String?),
          categorias: lista(j['categorias']).map(_lineaDe).toList(),
          principalDesvio: j['principalDesvio'] == null ? null : _lineaDe(j['principalDesvio'] as Map<String, dynamic>),
        );
      });

  @override
  Future<Result<ReporteMayordomia>> mayordomia(String? periodo) => intentar(() async {
        final r = await _dio.get<Map<String, dynamic>>('${ApiEndpoints.reportes}/mayordomia',
            queryParameters: {'periodo': ?periodo});
        final j = r.data!;
        final habitos = <Dimension, List<HabitoCumplimiento>>{};
        (j['habitos'] as Map<String, dynamic>? ?? const {}).forEach((dim, items) {
          habitos[Dimension.desdeApi(dim)] = lista(items)
              .map((h) => HabitoCumplimiento(
                    codigo: h['codigo'] as String,
                    nombre: h['nombre'] as String,
                    cumplimientoPct: dbl(h['cumplimientoPct']),
                    fuente: h['fuente'] as String?,
                  ))
              .toList();
        });
        return ReporteMayordomia(
          periodo: j['periodo'] as String,
          puntajes: {
            Dimension.tiempo: dblN(j['tiempo']),
            Dimension.talento: dblN(j['talento']),
            Dimension.tesoro: dblN(j['tesoro']),
            Dimension.templo: dblN(j['templo']),
          },
          global: dblN(j['global']),
          semaforo: Semaforo.desdeApi(j['semaforo'] as String?),
          habitos: habitos,
          diezmoAlDia: j['diezmoAlDia'] as bool?,
        );
      });
}
