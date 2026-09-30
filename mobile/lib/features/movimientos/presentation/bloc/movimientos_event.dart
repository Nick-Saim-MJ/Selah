part of 'movimientos_bloc.dart';

sealed class MovimientosEvent extends Equatable {
  const MovimientosEvent();

  @override
  List<Object?> get props => [];
}

/// Carga (o recarga) la primera página con el filtro actual.
final class MovimientosSolicitados extends MovimientosEvent {
  const MovimientosSolicitados();
}

final class MovimientosFiltroCambiado extends MovimientosEvent {
  const MovimientosFiltroCambiado(this.tipo);

  /// `null` = todos.
  final TipoMovimiento? tipo;

  @override
  List<Object?> get props => [tipo];
}

/// Scroll infinito: siguiente página.
final class MovimientosMasSolicitados extends MovimientosEvent {
  const MovimientosMasSolicitados();
}

/// Borra (lógicamente) un movimiento desde su detalle.
final class MovimientoEliminacionSolicitada extends MovimientosEvent {
  const MovimientoEliminacionSolicitada(this.id);

  final String id;

  @override
  List<Object?> get props => [id];
}
