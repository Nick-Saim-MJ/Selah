import '../../../../core/error/failures.dart';
import '../../../../core/error/result.dart';
import '../../../../core/usecase/usecase.dart';
import '../entities/habitos_entities.dart';
import '../repositories/habitos_repository.dart';

class ObtenerHabitos implements UseCase<List<HabitoDelDia>, SinParametros> {
  const ObtenerHabitos(this._repo);

  final HabitosRepository _repo;

  @override
  Future<Result<List<HabitoDelDia>>> call(SinParametros p) => _repo.delDia();
}

class RegistrarHabito implements UseCase<HabitoDelDia, RegistroHabitoParams> {
  const RegistrarHabito(this._repo);

  final HabitosRepository _repo;

  @override
  Future<Result<HabitoDelDia>> call(RegistroHabitoParams p) {
    if (p.valor < 0) {
      return Future.value(const Fallo(ValidacionFailure('El valor no puede ser negativo')));
    }
    return _repo.registrar(p.codigo, p.valor);
  }
}

class RegistroHabitoParams {
  const RegistroHabitoParams({required this.codigo, required this.valor});

  final String codigo;
  final double valor;
}
