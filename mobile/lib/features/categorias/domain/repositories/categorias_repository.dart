import '../../../../core/error/result.dart';
import '../entities/categorias_entities.dart';

abstract interface class CategoriasRepository {
  Future<Result<List<CategoriaGasto>>> categorias();

  /// Crea (id vacío) o actualiza.
  Future<Result<CategoriaGasto>> guardarCategoria(CategoriaGasto categoria);

  Future<Result<void>> ocultarCategoria(String id);

  Future<Result<List<FuenteIngreso>>> fuentes();

  Future<Result<FuenteIngreso>> guardarFuente(FuenteIngreso fuente);

  Future<Result<void>> ocultarFuente(String id);
}
