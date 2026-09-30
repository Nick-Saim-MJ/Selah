import 'package:dio/dio.dart';

import '../../../core/error/result.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/safe_call.dart';
import '../../../core/utils/json_utils.dart';
import '../domain/entities/categorias_entities.dart';
import '../domain/repositories/categorias_repository.dart';

CategoriaGasto _categoriaDe(Map<String, dynamic> j) => CategoriaGasto(
      id: j['id'] as String,
      nombre: j['nombre'] as String,
      presupuestoMensual: dbl(j['presupuestoMensual']),
      color: j['color'] as String?,
      orden: (j['orden'] as num?)?.toInt() ?? 0,
    );

FuenteIngreso _fuenteDe(Map<String, dynamic> j) => FuenteIngreso(
      id: j['id'] as String,
      nombre: j['nombre'] as String,
      montoEstimadoMensual: dbl(j['montoEstimadoMensual']),
    );

class CategoriasRepositoryImpl implements CategoriasRepository {
  const CategoriasRepositoryImpl(this._dio);

  final Dio _dio;

  @override
  Future<Result<List<CategoriaGasto>>> categorias() => intentar(() async {
        final r = await _dio.get<List<dynamic>>(ApiEndpoints.categorias);
        return lista(r.data).map(_categoriaDe).toList();
      });

  @override
  Future<Result<CategoriaGasto>> guardarCategoria(CategoriaGasto c) => intentar(() async {
        final datos = {
          'nombre': c.nombre.trim(),
          'tipo': 'GASTO',
          'color': c.color,
          'presupuestoMensual': c.presupuestoMensual,
        };
        final r = c.id.isEmpty
            ? await _dio.post<Map<String, dynamic>>(ApiEndpoints.categorias, data: datos)
            : await _dio.put<Map<String, dynamic>>('${ApiEndpoints.categorias}/${c.id}', data: datos);
        return _categoriaDe(r.data!);
      });

  @override
  Future<Result<void>> ocultarCategoria(String id) => intentar(() => _dio.delete<void>('${ApiEndpoints.categorias}/$id'));

  @override
  Future<Result<List<FuenteIngreso>>> fuentes() => intentar(() async {
        final r = await _dio.get<List<dynamic>>(ApiEndpoints.fuentesIngreso);
        return lista(r.data).map(_fuenteDe).toList();
      });

  @override
  Future<Result<FuenteIngreso>> guardarFuente(FuenteIngreso f) => intentar(() async {
        final datos = {'nombre': f.nombre.trim(), 'montoEstimadoMensual': f.montoEstimadoMensual};
        final r = f.id.isEmpty
            ? await _dio.post<Map<String, dynamic>>(ApiEndpoints.fuentesIngreso, data: datos)
            : await _dio.put<Map<String, dynamic>>('${ApiEndpoints.fuentesIngreso}/${f.id}', data: datos);
        return _fuenteDe(r.data!);
      });

  @override
  Future<Result<void>> ocultarFuente(String id) =>
      intentar(() => _dio.delete<void>('${ApiEndpoints.fuentesIngreso}/$id'));
}
