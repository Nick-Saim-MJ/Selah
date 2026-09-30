import '../../../../core/error/failures.dart';
import '../../../../core/error/result.dart';
import '../../../../core/usecase/usecase.dart';
import '../entities/mayordomia_entities.dart';
import '../repositories/mayordomia_repository.dart';

class ObtenerDiezmos implements UseCase<DiezmosVista, SinParametros> {
  const ObtenerDiezmos(this._repo);

  final MayordomiaRepository _repo;

  @override
  Future<Result<DiezmosVista>> call(SinParametros p) => _repo.diezmos();
}

class EntregarMayordomia implements UseCase<EntregaResultado, EntregaParams> {
  const EntregarMayordomia(this._repo);

  final MayordomiaRepository _repo;

  @override
  Future<Result<EntregaResultado>> call(EntregaParams p) => _repo.entregar(p.tipo, p.periodo);
}

class ObtenerConfigMayordomia implements UseCase<ConfigMayordomia, SinParametros> {
  const ObtenerConfigMayordomia(this._repo);

  final MayordomiaRepository _repo;

  @override
  Future<Result<ConfigMayordomia>> call(SinParametros p) => _repo.configuracion();
}

class GuardarConfigMayordomia implements UseCase<ConfigMayordomia, ConfigMayordomia> {
  const GuardarConfigMayordomia(this._repo);

  final MayordomiaRepository _repo;

  @override
  Future<Result<ConfigMayordomia>> call(ConfigMayordomia c) {
    if (c.pctDiezmo < 0 || c.pctDiezmo > 100 || c.pctOfrenda < 0 || c.pctOfrenda > 100) {
      return Future.value(const Fallo(ValidacionFailure('Los porcentajes deben estar entre 0 y 100')));
    }
    if (c.pctDiezmo + (c.ofrendaActiva ? c.pctOfrenda : 0) > 100) {
      return Future.value(const Fallo(ValidacionFailure('Diezmo + ofrenda no puede superar el 100%')));
    }
    return _repo.guardarConfiguracion(c);
  }
}

class EntregaParams {
  const EntregaParams({required this.tipo, required this.periodo});

  final TipoApartado tipo;
  final String periodo;
}
