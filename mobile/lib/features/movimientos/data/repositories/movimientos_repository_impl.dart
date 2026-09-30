import 'package:dio/dio.dart';

import '../../../../core/error/result.dart';
import '../../../../core/network/api_error_mapper.dart';
import '../../domain/entities/movimiento.dart';
import '../../domain/entities/tipo_movimiento.dart';
import '../../domain/repositories/movimientos_repository.dart';
import '../datasources/movimientos_remote_data_source.dart';

class MovimientosRepositoryImpl implements MovimientosRepository {
  const MovimientosRepositoryImpl(this._remote);

  final MovimientosRemoteDataSource _remote;

  @override
  Future<Result<PaginaMovimientos>> obtener({TipoMovimiento? tipo, int pagina = 0, int tamanio = 20}) =>
      _intentar(() => _remote.obtener(tipo: tipo, pagina: pagina, tamanio: tamanio));

  @override
  Future<Result<Movimiento>> registrar(NuevoMovimiento movimiento) => _intentar(() => _remote.registrar(movimiento));

  @override
  Future<Result<void>> eliminar(String id) => _intentar(() => _remote.eliminar(id));

  Future<Result<T>> _intentar<T>(Future<T> Function() llamada) async {
    try {
      return Exito(await llamada());
    } on DioException catch (e) {
      return Fallo(mapearDioException(e));
    }
  }
}
