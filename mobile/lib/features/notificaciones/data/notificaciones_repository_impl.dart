import 'package:dio/dio.dart';

import '../../../core/error/result.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/safe_call.dart';
import '../../../core/utils/json_utils.dart';
import '../domain/notificaciones_domain.dart';

PreferenciasNotificacion _prefsDe(Map<String, dynamic> j) => PreferenciasNotificacion(
      recordatorioDiezmo: j['recordatorioDiezmo'] as bool,
      recordatorioDeudas: j['recordatorioDeudas'] as bool,
      resumenSemanal: j['resumenSemanal'] as bool,
      reflexionSabado: j['reflexionSabado'] as bool,
    );

class NotificacionesRepositoryImpl implements NotificacionesRepository {
  const NotificacionesRepositoryImpl(this._dio);

  final Dio _dio;

  @override
  Future<Result<Bandeja>> bandeja() => intentar(() async {
        final j = (await _dio.get<Map<String, dynamic>>(ApiEndpoints.notificaciones)).data!;
        return Bandeja(
          noLeidas: (j['noLeidas'] as num).toInt(),
          items: lista(j['items'])
              .map((n) => Notificacion(
                    id: n['id'] as String,
                    tipo: n['tipo'] as String,
                    categoria: n['categoria'] as String,
                    titulo: n['titulo'] as String,
                    cuerpo: n['cuerpo'] as String?,
                    ruta: (n['datos'] as Map<String, dynamic>?)?['ruta'] as String?,
                    fecha: DateTime.parse(n['fecha'] as String).toLocal(),
                    leida: n['leida'] as bool,
                  ))
              .toList(),
        );
      });

  @override
  Future<Result<void>> marcarLeida(String id) => intentar(() => _dio.post<void>('${ApiEndpoints.notificaciones}/$id/leida'));

  @override
  Future<Result<void>> marcarTodasLeidas() => intentar(() => _dio.post<void>('${ApiEndpoints.notificaciones}/leidas'));

  @override
  Future<Result<PreferenciasNotificacion>> preferencias() => intentar(
      () async => _prefsDe((await _dio.get<Map<String, dynamic>>('${ApiEndpoints.notificaciones}/preferencias')).data!));

  @override
  Future<Result<PreferenciasNotificacion>> guardarPreferencias(PreferenciasNotificacion p) => intentar(() async {
        final r = await _dio.put<Map<String, dynamic>>('${ApiEndpoints.notificaciones}/preferencias', data: {
          'recordatorioDiezmo': p.recordatorioDiezmo,
          'recordatorioDeudas': p.recordatorioDeudas,
          'resumenSemanal': p.resumenSemanal,
          'reflexionSabado': p.reflexionSabado,
        });
        return _prefsDe(r.data!);
      });
}
