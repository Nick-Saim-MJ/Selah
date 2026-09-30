import 'package:equatable/equatable.dart';

/// Categoría de gasto editable con su presupuesto mensual.
class CategoriaGasto extends Equatable {
  const CategoriaGasto({
    required this.id,
    required this.nombre,
    required this.presupuestoMensual,
    this.color,
    this.orden = 0,
  });

  /// Id vacío = categoría nueva (aún no existe en el servidor).
  final String id;
  final String nombre;
  final double presupuestoMensual;

  /// #RRGGBB
  final String? color;
  final int orden;

  @override
  List<Object?> get props => [id, nombre, presupuestoMensual, color, orden];
}

/// Origen de ingresos con su monto mensual estimado.
class FuenteIngreso extends Equatable {
  const FuenteIngreso({required this.id, required this.nombre, required this.montoEstimadoMensual});

  final String id;
  final String nombre;
  final double montoEstimadoMensual;

  @override
  List<Object?> get props => [id, nombre, montoEstimadoMensual];
}
