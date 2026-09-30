import '../../../../core/error/result.dart';
import '../../../../core/usecase/usecase.dart';
import '../repositories/movimientos_repository.dart';

/// Borrado lógico: el movimiento desaparece de las listas, pero un registro financiero no se pierde.
class EliminarMovimiento implements UseCase<void, String> {
  const EliminarMovimiento(this._repository);

  final MovimientosRepository _repository;

  @override
  Future<Result<void>> call(String id) => _repository.eliminar(id);
}
