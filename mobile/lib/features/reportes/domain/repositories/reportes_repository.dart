import '../../../../core/error/result.dart';
import '../entities/reportes_entities.dart';

abstract interface class ReportesRepository {
  /// `periodo` = YYYY-MM; null = el mes actual.
  Future<Result<ReporteFinanciero>> financiero(String? periodo);

  Future<Result<ReporteMayordomia>> mayordomia(String? periodo);
}
