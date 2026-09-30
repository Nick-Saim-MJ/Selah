import '../../../../core/error/result.dart';
import '../entities/movimiento.dart';
import '../entities/tipo_movimiento.dart';

abstract interface class MovimientosRepository {
  Future<Result<PaginaMovimientos>> obtener({TipoMovimiento? tipo, int pagina = 0, int tamanio = 20});

  Future<Result<Movimiento>> registrar(NuevoMovimiento movimiento);

  Future<Result<void>> eliminar(String id);
}
