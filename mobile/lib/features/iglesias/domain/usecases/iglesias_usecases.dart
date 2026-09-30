import '../../../../core/error/failures.dart';
import '../../../../core/error/result.dart';
import '../../../../core/usecase/usecase.dart';
import '../entities/iglesia.dart';
import '../repositories/iglesias_repository.dart';

class ObtenerIglesiasActivas implements UseCase<List<Iglesia>, SinParametros> {
  const ObtenerIglesiasActivas(this._repository);

  final IglesiasRepository _repository;

  @override
  Future<Result<List<Iglesia>>> call(SinParametros p) => _repository.activas();
}

class ObtenerTodasLasIglesias implements UseCase<List<Iglesia>, SinParametros> {
  const ObtenerTodasLasIglesias(this._repository);

  final IglesiasRepository _repository;

  @override
  Future<Result<List<Iglesia>>> call(SinParametros p) => _repository.todas();
}

class GuardarIglesia implements UseCase<Iglesia, Iglesia> {
  const GuardarIglesia(this._repository);

  final IglesiasRepository _repository;

  /// Una iglesia sin id ('') se crea; con id se actualiza.
  @override
  Future<Result<Iglesia>> call(Iglesia i) {
    if (i.nombre.trim().isEmpty) {
      return Future.value(const Fallo(ValidacionFailure('La iglesia necesita un nombre')));
    }
    return i.id.isEmpty
        ? _repository.crear(nombre: i.nombre.trim(), ciudad: i.ciudad, distrito: i.distrito)
        : _repository.actualizar(i);
  }
}
