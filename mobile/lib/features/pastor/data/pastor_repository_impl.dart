import 'package:dio/dio.dart';

import '../../../core/domain/dimension.dart';
import '../../../core/domain/semaforo.dart';
import '../../../core/error/result.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/safe_call.dart';
import '../../../core/utils/json_utils.dart';
import '../domain/pastor_domain.dart';

class PastorRepositoryImpl implements PastorRepository {
  const PastorRepositoryImpl(this._dio);

  final Dio _dio;

  @override
  Future<Result<VistaCongregacion>> congregacion(String periodo) => intentar(() async {
        final respuestas = await Future.wait([
          _dio.get<Map<String, dynamic>>('${ApiEndpoints.pastor}/congregacion', queryParameters: {'periodo': periodo}),
          _dio.get<List<dynamic>>('${ApiEndpoints.pastor}/reportes-compartidos', queryParameters: {'periodo': periodo}),
        ]);
        final r = respuestas[0].data! as Map<String, dynamic>;
        final compartidos = lista(respuestas[1].data);
        return VistaCongregacion(
          resumen: ResumenCongregacion(
            periodo: r['periodo'] as String,
            miembrosActivos: (r['miembrosActivos'] as num).toInt(),
            miembrosConReporte: (r['miembrosConReporte'] as num).toInt(),
            minimoAnonimato: (r['minimoAnonimato'] as num).toInt(),
            datosSuficientes: r['datosSuficientes'] as bool,
            fidelidadDiezmoPct: dblN(r['fidelidadDiezmoPct']),
            tiempo: dblN(r['tiempo']),
            talento: dblN(r['talento']),
            tesoro: dblN(r['tesoro']),
            templo: dblN(r['templo']),
            global: dblN(r['global']),
          ),
          compartidos: compartidos.map(_compartidoDe).toList(),
        );
      });

  ReporteCompartido _compartidoDe(Map<String, dynamic> j) {
    final habitos = <Dimension, List<HabitoCompartido>>{};
    (j['habitos'] as Map<String, dynamic>? ?? const {}).forEach((dim, items) {
      habitos[Dimension.desdeApi(dim)] = lista(items)
          .map((h) => HabitoCompartido(nombre: h['nombre'] as String, cumplimientoPct: dbl(h['cumplimientoPct'])))
          .toList();
    });
    return ReporteCompartido(
      usuarioId: j['usuarioId'] as String,
      nombre: j['nombre'] as String,
      puntajes: {
        Dimension.tiempo: dblN(j['tiempo']),
        Dimension.talento: dblN(j['talento']),
        Dimension.tesoro: dblN(j['tesoro']),
        Dimension.templo: dblN(j['templo']),
      },
      global: dblN(j['global']),
      semaforo: Semaforo.desdeApi(j['semaforo'] as String?),
      diezmoAlDia: j['diezmoAlDia'] as bool?,
      habitos: habitos,
    );
  }
}
