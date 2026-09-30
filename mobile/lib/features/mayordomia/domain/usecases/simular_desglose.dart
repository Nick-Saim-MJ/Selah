import '../../../../core/error/result.dart';
import '../../../../core/usecase/usecase.dart';
import '../entities/desglose_ingreso.dart';
import '../repositories/mayordomia_repository.dart';

class SimularDesglose implements UseCase<DesgloseIngreso, double> {
  const SimularDesglose(this._repository);

  final MayordomiaRepository _repository;

  @override
  Future<Result<DesgloseIngreso>> call(double montoIngreso) => _repository.simular(montoIngreso);
}
