import '../../../../core/error/result.dart';
import '../entities/desglose_ingreso.dart';
import '../entities/mayordomia_entities.dart';

abstract interface class MayordomiaRepository {
  /// Vista previa del diezmo/ofrenda de un ingreso (no guarda nada).
  Future<Result<DesgloseIngreso>> simular(double montoIngreso);

  Future<Result<DiezmosVista>> diezmos({int meses = 6});

  /// Marca como entregado todo lo pendiente del tipo en el mes (`periodo` = YYYY-MM).
  Future<Result<EntregaResultado>> entregar(TipoApartado tipo, String periodo);

  Future<Result<ConfigMayordomia>> configuracion();

  Future<Result<ConfigMayordomia>> guardarConfiguracion(ConfigMayordomia configuracion);
}
