import 'package:dio/dio.dart';

import '../../../core/error/result.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/safe_call.dart';
import '../../../core/utils/formato.dart';
import '../../../core/utils/json_utils.dart';
import '../domain/entities/metas_entities.dart';
import '../domain/repositories/metas_repository.dart';

MetaAhorro _metaDe(Map<String, dynamic> j) => MetaAhorro(
      id: j['id'] as String,
      nombre: j['nombre'] as String,
      proposito: j['proposito'] as String,
      tipo: TipoMeta.desdeApi(j['tipo'] as String),
      montoObjetivo: dbl(j['montoObjetivo']),
      montoActual: dbl(j['montoActual']),
      porcentaje: dbl(j['porcentaje']),
      restante: dbl(j['restante']),
      fechaObjetivo: fechaN(j['fechaObjetivo']),
      aporteMensualSugerido: dblN(j['aporteMensualSugerido']),
      esPrincipal: j['esPrincipal'] as bool,
      estado: j['estado'] as String,
    );

Deuda _deudaDe(Map<String, dynamic> j) => Deuda(
      id: j['id'] as String,
      nombre: j['nombre'] as String,
      acreedor: j['acreedor'] as String?,
      montoOriginal: dbl(j['montoOriginal']),
      saldoActual: dbl(j['saldoActual']),
      tasaAnual: dbl(j['tasaAnual']),
      plazoMeses: (j['plazoMeses'] as num).toInt(),
      cuotaMensual: dbl(j['cuotaMensual']),
      mesesRestantes: (j['mesesRestantes'] as num?)?.toInt(),
      porcentajePagado: dbl(j['porcentajePagado']),
      diaPago: (j['diaPago'] as num?)?.toInt(),
    );

class MetasRepositoryImpl implements MetasRepository {
  const MetasRepositoryImpl(this._dio);

  final Dio _dio;

  @override
  Future<Result<List<MetaAhorro>>> metas() => intentar(() async {
        final r = await _dio.get<List<dynamic>>(ApiEndpoints.metas);
        return lista(r.data).map(_metaDe).toList();
      });

  @override
  Future<Result<MetaAhorro>> guardarMeta(MetaAhorro m) => intentar(() async {
        final datos = {
          'nombre': m.nombre.trim(),
          'proposito': m.proposito.trim(),
          'tipo': m.tipo.api,
          'montoObjetivo': m.montoObjetivo,
          'fechaObjetivo': m.fechaObjetivo == null ? null : Formato.fechaApi(m.fechaObjetivo!),
        };
        final r = m.id.isEmpty
            ? await _dio.post<Map<String, dynamic>>(ApiEndpoints.metas, data: datos)
            : await _dio.put<Map<String, dynamic>>('${ApiEndpoints.metas}/${m.id}', data: datos);
        return _metaDe(r.data!);
      });

  @override
  Future<Result<MetaAhorro>> hacerPrincipal(String id) => intentar(() async {
        final r = await _dio.post<Map<String, dynamic>>('${ApiEndpoints.metas}/$id/principal');
        return _metaDe(r.data!);
      });

  @override
  Future<Result<void>> cancelarMeta(String id) => intentar(() => _dio.delete<void>('${ApiEndpoints.metas}/$id'));

  @override
  Future<Result<ResumenDeudas>> deudas() => intentar(() async {
        final r = await _dio.get<Map<String, dynamic>>(ApiEndpoints.deudas);
        final j = r.data!;
        return ResumenDeudas(
          deudas: lista(j['deudas']).map(_deudaDe).toList(),
          cuotaTotalMensual: dbl(j['cuotaTotalMensual']),
          ingresoMensualBase: dbl(j['ingresoMensualBase']),
          porcentajeCuotasSobreIngreso: dbl(j['porcentajeCuotasSobreIngreso']),
          superaLimite: j['superaLimite'] as bool,
          limitePorcentaje: dbl(j['limitePorcentaje']),
        );
      });

  @override
  Future<Result<Deuda>> crearDeuda(NuevaDeuda d) => intentar(() async {
        final r = await _dio.post<Map<String, dynamic>>(ApiEndpoints.deudas, data: {
          'nombre': d.nombre.trim(),
          'acreedor': d.acreedor,
          'montoOriginal': d.montoOriginal,
          'saldoActual': d.saldoActual,
          'tasaAnual': d.tasaAnual,
          'plazoMeses': d.plazoMeses,
          'diaPago': d.diaPago,
        });
        return _deudaDe(r.data!);
      });
}
