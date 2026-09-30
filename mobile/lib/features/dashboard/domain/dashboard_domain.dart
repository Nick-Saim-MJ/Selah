import '../../../core/error/result.dart';
import '../../../core/usecase/usecase.dart';
import 'entities/inicio.dart';

abstract interface class InicioRepository {
  Future<Result<Inicio>> inicio();
}

class ObtenerInicio implements UseCase<Inicio, SinParametros> {
  const ObtenerInicio(this._repo);

  final InicioRepository _repo;

  @override
  Future<Result<Inicio>> call(SinParametros p) => _repo.inicio();
}
