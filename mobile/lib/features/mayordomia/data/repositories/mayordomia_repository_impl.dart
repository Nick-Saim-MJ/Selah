import 'package:dio/dio.dart';

import '../../../../core/error/result.dart';
import '../../../../core/network/api_client.dart';
import '../../../../core/network/safe_call.dart';
import '../../../../core/utils/json_utils.dart';
import '../../domain/entities/desglose_ingreso.dart';
import '../../domain/entities/mayordomia_entities.dart';
import '../../domain/repositories/mayordomia_repository.dart';

ResumenMayordomia _resumenDe(Map<String, dynamic> j) => ResumenMayordomia(
      periodo: j['periodo'] as String,
      diezmoApartado: dbl(j['diezmoApartado']),
      diezmoPendiente: dbl(j['diezmoPendiente']),
      ofrendaApartada: dbl(j['ofrendaApartada']),
      ofrendaPendiente: dbl(j['ofrendaPendiente']),
      diezmoAlDia: j['diezmoAlDia'] as bool,
    );

ConfigMayordomia _configDe(Map<String, dynamic> j) => ConfigMayordomia(
      pctDiezmo: dbl(j['pctDiezmo']),
      ofrendaActiva: j['ofrendaActiva'] as bool,
      pctOfrenda: dbl(j['pctOfrenda']),
      diaEntregaDiezmo: (j['diaEntregaDiezmo'] as num?)?.toInt(),
      modoSabadoActivo: j['modoSabadoActivo'] as bool,
    );

class MayordomiaRepositoryImpl implements MayordomiaRepository {
  const MayordomiaRepositoryImpl(this._dio);

  final Dio _dio;

  @override
  Future<Result<DesgloseIngreso>> simular(double montoIngreso) => intentar(() async {
        final r = await _dio.post<Map<String, dynamic>>(
          ApiEndpoints.simulacionMayordomia,
          data: {'monto': double.parse(montoIngreso.toStringAsFixed(2))},
        );
        final j = r.data!;
        return DesgloseIngreso(
          ingreso: dbl(j['ingreso']),
          pctDiezmo: dbl(j['pctDiezmo']),
          diezmo: dbl(j['diezmo']),
          pctOfrenda: dbl(j['pctOfrenda']),
          ofrenda: dbl(j['ofrenda']),
          disponible: dbl(j['disponible']),
        );
      });

  @override
  Future<Result<DiezmosVista>> diezmos({int meses = 6}) => intentar(() async {
        final r = await _dio.get<List<dynamic>>(ApiEndpoints.historialMayordomia, queryParameters: {'meses': meses});
        final historial = lista(r.data).map(_resumenDe).toList();
        return DiezmosVista(mesActual: historial.last, historial: historial);
      });

  @override
  Future<Result<EntregaResultado>> entregar(TipoApartado tipo, String periodo) => intentar(() async {
        final r = await _dio.post<Map<String, dynamic>>(ApiEndpoints.entregasMayordomia,
            data: {'tipo': tipo.api, 'periodo': periodo});
        return EntregaResultado(
          montoEntregado: dbl(r.data!['montoEntregado']),
          apartadosEntregados: (r.data!['apartadosEntregados'] as num).toInt(),
        );
      });

  @override
  Future<Result<ConfigMayordomia>> configuracion() => intentar(() async {
        final r = await _dio.get<Map<String, dynamic>>(ApiEndpoints.configuracionMayordomia);
        return _configDe(r.data!);
      });

  @override
  Future<Result<ConfigMayordomia>> guardarConfiguracion(ConfigMayordomia c) => intentar(() async {
        final r = await _dio.put<Map<String, dynamic>>(ApiEndpoints.configuracionMayordomia, data: {
          'pctDiezmo': c.pctDiezmo,
          'ofrendaActiva': c.ofrendaActiva,
          'pctOfrenda': c.pctOfrenda,
          'diaEntregaDiezmo': c.diaEntregaDiezmo,
          'modoSabadoActivo': c.modoSabadoActivo,
        });
        return _configDe(r.data!);
      });
}
