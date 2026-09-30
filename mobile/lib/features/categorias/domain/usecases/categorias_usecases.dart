import '../../../../core/error/failures.dart';
import '../../../../core/error/result.dart';
import '../../../../core/usecase/usecase.dart';
import '../entities/categorias_entities.dart';
import '../repositories/categorias_repository.dart';

class ObtenerCategorias implements UseCase<List<CategoriaGasto>, SinParametros> {
  const ObtenerCategorias(this._repo);

  final CategoriasRepository _repo;

  @override
  Future<Result<List<CategoriaGasto>>> call(SinParametros p) => _repo.categorias();
}

class GuardarCategoria implements UseCase<CategoriaGasto, CategoriaGasto> {
  const GuardarCategoria(this._repo);

  final CategoriasRepository _repo;

  @override
  Future<Result<CategoriaGasto>> call(CategoriaGasto c) {
    if (c.nombre.trim().isEmpty) {
      return Future.value(const Fallo(ValidacionFailure('La categoría necesita un nombre')));
    }
    if (c.presupuestoMensual < 0) {
      return Future.value(const Fallo(ValidacionFailure('El presupuesto no puede ser negativo')));
    }
    return _repo.guardarCategoria(c);
  }
}

class OcultarCategoria implements UseCase<void, String> {
  const OcultarCategoria(this._repo);

  final CategoriasRepository _repo;

  @override
  Future<Result<void>> call(String id) => _repo.ocultarCategoria(id);
}

class ObtenerFuentesIngreso implements UseCase<List<FuenteIngreso>, SinParametros> {
  const ObtenerFuentesIngreso(this._repo);

  final CategoriasRepository _repo;

  @override
  Future<Result<List<FuenteIngreso>>> call(SinParametros p) => _repo.fuentes();
}

class GuardarFuenteIngreso implements UseCase<FuenteIngreso, FuenteIngreso> {
  const GuardarFuenteIngreso(this._repo);

  final CategoriasRepository _repo;

  @override
  Future<Result<FuenteIngreso>> call(FuenteIngreso f) {
    if (f.nombre.trim().isEmpty) {
      return Future.value(const Fallo(ValidacionFailure('La fuente de ingreso necesita un nombre')));
    }
    if (f.montoEstimadoMensual < 0) {
      return Future.value(const Fallo(ValidacionFailure('El monto no puede ser negativo')));
    }
    return _repo.guardarFuente(f);
  }
}

class OcultarFuenteIngreso implements UseCase<void, String> {
  const OcultarFuenteIngreso(this._repo);

  final CategoriasRepository _repo;

  @override
  Future<Result<void>> call(String id) => _repo.ocultarFuente(id);
}
