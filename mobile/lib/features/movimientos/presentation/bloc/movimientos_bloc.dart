import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../domain/entities/movimiento.dart';
import '../../domain/entities/tipo_movimiento.dart';
import '../../domain/usecases/eliminar_movimiento.dart';
import '../../domain/usecases/obtener_movimientos.dart';

part 'movimientos_event.dart';
part 'movimientos_state.dart';

/// Lista paginada de movimientos con filtro por tipo.
class MovimientosBloc extends Bloc<MovimientosEvent, MovimientosState> {
  MovimientosBloc(this._obtenerMovimientos, this._eliminarMovimiento) : super(const MovimientosState()) {
    on<MovimientosSolicitados>(_alSolicitar);
    on<MovimientosFiltroCambiado>(_alFiltrar);
    on<MovimientosMasSolicitados>(_alPedirMas);
    on<MovimientoEliminacionSolicitada>(_alEliminar);
  }

  final ObtenerMovimientos _obtenerMovimientos;
  final EliminarMovimiento _eliminarMovimiento;

  Future<void> _alSolicitar(MovimientosSolicitados event, Emitter<MovimientosState> emit) =>
      _cargarPrimeraPagina(state.filtro, emit);

  Future<void> _alFiltrar(MovimientosFiltroCambiado event, Emitter<MovimientosState> emit) =>
      _cargarPrimeraPagina(event.tipo, emit);

  Future<void> _cargarPrimeraPagina(TipoMovimiento? tipo, Emitter<MovimientosState> emit) async {
    emit(MovimientosState(estado: EstadoCarga.cargando, filtro: tipo, items: state.filtro == tipo ? state.items : const []));
    final r = await _obtenerMovimientos(FiltroMovimientos(tipo: tipo));
    emit(r.fold(
      (f) => state.copyWith(estado: EstadoCarga.fallo, error: f.mensaje),
      (p) => state.copyWith(estado: EstadoCarga.exito, items: p.items, pagina: p.pagina, hayMas: p.hayMas),
    ));
  }

  Future<void> _alPedirMas(MovimientosMasSolicitados event, Emitter<MovimientosState> emit) async {
    if (!state.hayMas || state.estado == EstadoCarga.cargando) return;
    emit(state.copyWith(estado: EstadoCarga.cargando));
    final r = await _obtenerMovimientos(FiltroMovimientos(tipo: state.filtro, pagina: state.pagina + 1));
    emit(r.fold(
      (f) => state.copyWith(estado: EstadoCarga.fallo, error: f.mensaje),
      (p) => state.copyWith(
        estado: EstadoCarga.exito,
        items: [...state.items, ...p.items],
        pagina: p.pagina,
        hayMas: p.hayMas,
      ),
    ));
  }

  /// Quita el movimiento de la lista si el servidor lo eliminó; si no, avisa del error y conserva la lista.
  Future<void> _alEliminar(MovimientoEliminacionSolicitada event, Emitter<MovimientosState> emit) async {
    final r = await _eliminarMovimiento(event.id);
    emit(r.fold(
      (f) => state.copyWith(error: f.mensaje),
      (_) => state.copyWith(items: [for (final m in state.items) if (m.id != event.id) m], eliminado: true),
    ));
  }
}
