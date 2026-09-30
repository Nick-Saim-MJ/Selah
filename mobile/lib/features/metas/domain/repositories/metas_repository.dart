import '../../../../core/error/result.dart';
import '../entities/metas_entities.dart';

abstract interface class MetasRepository {
  Future<Result<List<MetaAhorro>>> metas();

  /// Crea (id vacío) o actualiza.
  Future<Result<MetaAhorro>> guardarMeta(MetaAhorro meta);

  Future<Result<MetaAhorro>> hacerPrincipal(String id);

  Future<Result<void>> cancelarMeta(String id);

  Future<Result<ResumenDeudas>> deudas();

  Future<Result<Deuda>> crearDeuda(NuevaDeuda deuda);
}
