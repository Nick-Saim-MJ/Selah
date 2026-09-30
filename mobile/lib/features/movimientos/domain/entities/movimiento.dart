import 'package:equatable/equatable.dart';

import 'tipo_movimiento.dart';

/// Movimiento de dinero. El monto es informativo en el cliente: el backend
/// calcula con BigDecimal y es la fuente de verdad.
class Movimiento extends Equatable {
  const Movimiento({
    required this.id,
    required this.tipo,
    required this.monto,
    required this.fecha,
    this.descripcion,
    this.categoriaId,
    this.metaId,
    this.deudaId,
    this.esPresupuestado = true,
  });

  final String id;
  final TipoMovimiento tipo;
  final double monto;
  final DateTime fecha;
  final String? descripcion;
  final String? categoriaId;
  final String? metaId;
  final String? deudaId;
  final bool esPresupuestado;

  /// Monto con signo desde la perspectiva del saldo (+ ingreso, − todo lo demás).
  double get montoConSigno => tipo.esEntrada ? monto : -monto;

  @override
  List<Object?> get props => [id, tipo, monto, fecha, descripcion, categoriaId, metaId, deudaId, esPresupuestado];
}

class NuevoMovimiento extends Equatable {
  const NuevoMovimiento({
    required this.tipo,
    required this.monto,
    required this.fecha,
    this.descripcion,
    this.categoriaId,
    this.metaId,
    this.deudaId,
    this.esPresupuestado = true,
  });

  final TipoMovimiento tipo;
  final double monto;
  final DateTime fecha;
  final String? descripcion;
  final String? categoriaId;
  final String? metaId;
  final String? deudaId;
  final bool esPresupuestado;

  @override
  List<Object?> get props => [tipo, monto, fecha, descripcion, categoriaId, metaId, deudaId, esPresupuestado];
}

class PaginaMovimientos extends Equatable {
  const PaginaMovimientos({required this.items, required this.pagina, required this.totalPaginas});

  final List<Movimiento> items;
  final int pagina;
  final int totalPaginas;

  bool get hayMas => pagina + 1 < totalPaginas;

  @override
  List<Object?> get props => [items, pagina, totalPaginas];
}
