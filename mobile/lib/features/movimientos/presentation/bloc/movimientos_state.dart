part of 'movimientos_bloc.dart';

enum EstadoCarga { inicial, cargando, exito, fallo }

final class MovimientosState extends Equatable {
  const MovimientosState({
    this.estado = EstadoCarga.inicial,
    this.items = const [],
    this.filtro,
    this.pagina = 0,
    this.hayMas = false,
    this.error,
    this.eliminado = false,
  });

  final EstadoCarga estado;
  final List<Movimiento> items;
  final TipoMovimiento? filtro;
  final int pagina;
  final bool hayMas;
  final String? error;

  /// Se acaba de eliminar un movimiento (para mostrar la confirmación una sola vez).
  final bool eliminado;

  /// Agrupa por día para mostrar encabezados de fecha, como en el prototipo.
  Map<DateTime, List<Movimiento>> get porFecha {
    final grupos = <DateTime, List<Movimiento>>{};
    for (final m in items) {
      final dia = DateTime(m.fecha.year, m.fecha.month, m.fecha.day);
      (grupos[dia] ??= []).add(m);
    }
    return grupos;
  }

  MovimientosState copyWith({
    EstadoCarga? estado,
    List<Movimiento>? items,
    int? pagina,
    bool? hayMas,
    String? error,
    bool eliminado = false,
  }) =>
      MovimientosState(
        estado: estado ?? this.estado,
        items: items ?? this.items,
        filtro: filtro,
        pagina: pagina ?? this.pagina,
        hayMas: hayMas ?? this.hayMas,
        error: error,
        eliminado: eliminado,
      );

  @override
  List<Object?> get props => [estado, items, filtro, pagina, hayMas, error, eliminado];
}
