import '../../../../core/error/result.dart';
import '../../../../core/usecase/usecase.dart';
import '../entities/reportes_entities.dart';
import '../repositories/reportes_repository.dart';

/// `String?` = periodo YYYY-MM (null: mes actual).
class ObtenerReporteFinanciero implements UseCase<ReporteFinanciero, String?> {
  const ObtenerReporteFinanciero(this._repo);

  final ReportesRepository _repo;

  @override
  Future<Result<ReporteFinanciero>> call(String? periodo) => _repo.financiero(periodo);
}

class ObtenerReporteMayordomia implements UseCase<ReporteMayordomia, String?> {
  const ObtenerReporteMayordomia(this._repo);

  final ReportesRepository _repo;

  @override
  Future<Result<ReporteMayordomia>> call(String? periodo) => _repo.mayordomia(periodo);
}
