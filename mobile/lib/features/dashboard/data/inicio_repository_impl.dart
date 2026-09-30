import 'package:dio/dio.dart';

import '../../../core/domain/semaforo.dart';
import '../../../core/error/result.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/safe_call.dart';
import '../../../core/utils/json_utils.dart';
import '../domain/dashboard_domain.dart';
import '../domain/entities/inicio.dart';

class InicioRepositoryImpl implements InicioRepository {
  const InicioRepositoryImpl(this._dio);

  final Dio _dio;

  @override
  Future<Result<Inicio>> inicio() => intentar(() async {
        final j = (await _dio.get<Map<String, dynamic>>(ApiEndpoints.inicio)).data!;
        final desvio = j['principalDesvio'] as Map<String, dynamic>?;
        final diezmo = j['diezmo'] as Map<String, dynamic>;
        final meta = j['metaPrincipal'] as Map<String, dynamic>?;
        return Inicio(
          periodo: j['periodo'] as String,
          semaforo: Semaforo.desdeApi(j['semaforo'] as String?),
          ratioGasto: dbl(j['ratioGasto']),
          ratioDeuda: dbl(j['ratioDeuda']),
          ratioAhorro: dbl(j['ratioAhorro']),
          ingresos: dbl(j['ingresos']),
          gastos: dbl(j['gastos']),
          saldoDisponible: dbl(j['saldoDisponible']),
          apartadoTotal: dbl(j['apartadoTotal']),
          categoriaDesvio: desvio?['categoria'] as String?,
          variacionDesvio: dblN(desvio?['variacionPct']),
          diezmoPendiente: dbl(diezmo['pendiente']),
          ofrendaPendiente: dbl(diezmo['ofrendaPendiente']),
          metaPrincipal: meta == null
              ? null
              : MetaResumen(
                  id: meta['id'] as String,
                  nombre: meta['nombre'] as String,
                  montoActual: dbl(meta['montoActual']),
                  montoObjetivo: dbl(meta['montoObjetivo']),
                  porcentaje: dbl(meta['porcentaje']),
                ),
          reflexionVisible: j['reflexionVisible'] as bool,
          reflexionRespondida: j['reflexionRespondida'] as bool,
          notificacionesNoLeidas: (j['notificacionesNoLeidas'] as num).toInt(),
        );
      });
}
