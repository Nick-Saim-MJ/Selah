import 'package:dio/dio.dart';

import '../../../../core/network/api_client.dart';
import '../../domain/entities/movimiento.dart';
import '../../domain/entities/tipo_movimiento.dart';
import '../models/movimiento_model.dart';

abstract interface class MovimientosRemoteDataSource {
  Future<PaginaMovimientos> obtener({TipoMovimiento? tipo, required int pagina, required int tamanio});

  Future<MovimientoModel> registrar(NuevoMovimiento movimiento);

  Future<void> eliminar(String id);
}

class MovimientosRemoteDataSourceImpl implements MovimientosRemoteDataSource {
  const MovimientosRemoteDataSourceImpl(this._dio);

  final Dio _dio;

  @override
  Future<PaginaMovimientos> obtener({TipoMovimiento? tipo, required int pagina, required int tamanio}) async {
    final r = await _dio.get<Map<String, dynamic>>(
      ApiEndpoints.movimientos,
      queryParameters: {'tipo': ?tipo?.api, 'pagina': pagina, 'tamanio': tamanio},
    );
    final data = r.data!;
    return PaginaMovimientos(
      items: (data['contenido'] as List).cast<Map<String, dynamic>>().map(MovimientoModel.fromJson).toList(),
      pagina: data['pagina'] as int,
      totalPaginas: data['totalPaginas'] as int,
    );
  }

  @override
  Future<MovimientoModel> registrar(NuevoMovimiento movimiento) async {
    final r = await _dio.post<Map<String, dynamic>>(
      ApiEndpoints.movimientos,
      data: MovimientoModel.nuevoToJson(movimiento),
    );
    return MovimientoModel.fromJson(r.data!);
  }

  @override
  Future<void> eliminar(String id) => _dio.delete<void>('${ApiEndpoints.movimientos}/$id');
}
