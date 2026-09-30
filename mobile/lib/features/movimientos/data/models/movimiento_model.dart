import '../../../../core/utils/formato.dart';
import '../../domain/entities/movimiento.dart';
import '../../domain/entities/tipo_movimiento.dart';

/// Mapeo JSON ↔ entidad. Contrato: MovimientoResponse del backend.
class MovimientoModel extends Movimiento {
  const MovimientoModel({
    required super.id,
    required super.tipo,
    required super.monto,
    required super.fecha,
    super.descripcion,
    super.categoriaId,
    super.metaId,
    super.deudaId,
    super.esPresupuestado,
  });

  factory MovimientoModel.fromJson(Map<String, dynamic> json) => MovimientoModel(
        id: json['id'] as String,
        tipo: TipoMovimiento.desdeApi(json['tipo'] as String),
        monto: (json['monto'] as num).toDouble(),
        fecha: DateTime.parse(json['fecha'] as String),
        descripcion: json['descripcion'] as String?,
        categoriaId: json['categoriaId'] as String?,
        metaId: json['metaId'] as String?,
        deudaId: json['deudaId'] as String?,
        esPresupuestado: json['esPresupuestado'] as bool? ?? true,
      );

  static Map<String, dynamic> nuevoToJson(NuevoMovimiento m) => {
        'tipo': m.tipo.api,
        'monto': double.parse(m.monto.toStringAsFixed(2)),
        'fecha': Formato.fechaApi(m.fecha),
        'descripcion': ?m.descripcion,
        'categoriaId': ?m.categoriaId,
        'metaId': ?m.metaId,
        'deudaId': ?m.deudaId,
        'esPresupuestado': m.esPresupuestado,
      };
}
