import '../../../../core/error/result.dart';
import '../entities/habitos_entities.dart';

abstract interface class HabitosRepository {
  Future<Result<List<HabitoDelDia>>> delDia();

  /// Registra o corrige el valor de hoy. Devuelve el hábito actualizado.
  Future<Result<HabitoDelDia>> registrar(String codigo, double valor);
}
