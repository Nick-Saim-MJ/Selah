import '../../../../core/error/failures.dart';
import '../../../../core/error/result.dart';
import '../../../../core/usecase/usecase.dart';
import '../entities/movimiento.dart';
import '../entities/tipo_movimiento.dart';
import '../repositories/movimientos_repository.dart';

/// Única vía de captura de dinero en la app (spec: "fuente única").
class RegistrarMovimiento implements UseCase<Movimiento, NuevoMovimiento> {
  const RegistrarMovimiento(this._repository);

  final MovimientosRepository _repository;

  @override
  Future<Result<Movimiento>> call(NuevoMovimiento m) async {
    // Validaciones rápidas en el cliente; el backend vuelve a validar todo.
    if (m.monto <= 0) {
      return const Fallo(ValidacionFailure('El monto debe ser mayor que cero'));
    }
    if (m.tipo == TipoMovimiento.gasto && m.categoriaId == null) {
      return const Fallo(ValidacionFailure('Elige una categoría para el gasto'));
    }
    if (m.tipo == TipoMovimiento.diezmo) {
      return const Fallo(ValidacionFailure('El diezmo se entrega desde la pestaña Diezmos'));
    }
    return _repository.registrar(m);
  }
}
