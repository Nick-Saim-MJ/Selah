import '../../../../core/error/result.dart';
import '../entities/iglesia.dart';

abstract interface class IglesiasRepository {
  /// Lista pública (para el registro): solo las activas.
  Future<Result<List<Iglesia>>> activas();

  /// Panel del admin: todas.
  Future<Result<List<Iglesia>>> todas();

  Future<Result<Iglesia>> crear({required String nombre, String? ciudad, String? distrito});

  Future<Result<Iglesia>> actualizar(Iglesia iglesia);
}
