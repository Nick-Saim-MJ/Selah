import '../../../../core/error/failures.dart';
import '../../../../core/error/result.dart';
import '../../../../core/usecase/usecase.dart';
import '../entities/metas_entities.dart';
import '../repositories/metas_repository.dart';

class ObtenerMetas implements UseCase<List<MetaAhorro>, SinParametros> {
  const ObtenerMetas(this._repo);

  final MetasRepository _repo;

  @override
  Future<Result<List<MetaAhorro>>> call(SinParametros p) => _repo.metas();
}

class GuardarMeta implements UseCase<MetaAhorro, MetaAhorro> {
  const GuardarMeta(this._repo);

  final MetasRepository _repo;

  @override
  Future<Result<MetaAhorro>> call(MetaAhorro m) {
    if (m.nombre.trim().isEmpty) {
      return Future.value(const Fallo(ValidacionFailure('La meta necesita un nombre')));
    }
    if (m.proposito.trim().isEmpty) {
      return Future.value(const Fallo(ValidacionFailure('Cuéntanos para qué ahorras: cada meta tiene un propósito')));
    }
    if (m.montoObjetivo <= 0) {
      return Future.value(const Fallo(ValidacionFailure('El monto objetivo debe ser mayor que cero')));
    }
    return _repo.guardarMeta(m);
  }
}

class MarcarMetaPrincipal implements UseCase<MetaAhorro, String> {
  const MarcarMetaPrincipal(this._repo);

  final MetasRepository _repo;

  @override
  Future<Result<MetaAhorro>> call(String id) => _repo.hacerPrincipal(id);
}

class CancelarMeta implements UseCase<void, String> {
  const CancelarMeta(this._repo);

  final MetasRepository _repo;

  @override
  Future<Result<void>> call(String id) => _repo.cancelarMeta(id);
}

class ObtenerDeudas implements UseCase<ResumenDeudas, SinParametros> {
  const ObtenerDeudas(this._repo);

  final MetasRepository _repo;

  @override
  Future<Result<ResumenDeudas>> call(SinParametros p) => _repo.deudas();
}

class CrearDeuda implements UseCase<Deuda, NuevaDeuda> {
  const CrearDeuda(this._repo);

  final MetasRepository _repo;

  @override
  Future<Result<Deuda>> call(NuevaDeuda d) {
    if (d.nombre.trim().isEmpty) {
      return Future.value(const Fallo(ValidacionFailure('La deuda necesita un nombre')));
    }
    if (d.montoOriginal <= 0) {
      return Future.value(const Fallo(ValidacionFailure('El monto debe ser mayor que cero')));
    }
    if (d.saldoActual != null && d.saldoActual! > d.montoOriginal) {
      return Future.value(const Fallo(ValidacionFailure('Lo que debes hoy no puede ser más que el monto original')));
    }
    if (d.plazoMeses <= 0) {
      return Future.value(const Fallo(ValidacionFailure('Indica en cuántos meses la pagarás')));
    }
    return _repo.crearDeuda(d);
  }
}
