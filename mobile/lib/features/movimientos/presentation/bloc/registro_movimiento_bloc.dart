import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/bloc/transformers.dart';
import '../../../../core/usecase/usecase.dart';
import '../../../categorias/domain/entities/categorias_entities.dart';
import '../../../categorias/domain/usecases/categorias_usecases.dart';
import '../../../mayordomia/domain/entities/desglose_ingreso.dart';
import '../../../mayordomia/domain/usecases/simular_desglose.dart';
import '../../../metas/domain/entities/metas_entities.dart';
import '../../../metas/domain/usecases/metas_usecases.dart';
import '../../../reportes/domain/entities/reportes_entities.dart';
import '../../../reportes/domain/usecases/reportes_usecases.dart';
import '../../domain/entities/movimiento.dart';
import '../../domain/entities/tipo_movimiento.dart';
import '../../domain/usecases/registrar_movimiento.dart';

part 'registro_movimiento_event.dart';
part 'registro_movimiento_state.dart';

/// Meta o deuda ya elegida al llegar desde Metas ("Aportar" / "Registrar pago"): no se duplica el formulario.
class PreseleccionMovimiento {
  const PreseleccionMovimiento({this.metaId, this.deudaId});

  final String? metaId;
  final String? deudaId;
}

/// Formulario de nuevo movimiento: la ÚNICA vía para registrar dinero (Lucas 16:10).
/// Para ingresos muestra el diezmo y la ofrenda en tiempo real ANTES de guardar (Prov. 3:9);
/// para gastos, cuánto queda del presupuesto de la categoría.
class RegistroMovimientoBloc extends Bloc<RegistroMovimientoEvent, RegistroMovimientoState> {
  RegistroMovimientoBloc({
    required TipoMovimiento tipo,
    required PreseleccionMovimiento preseleccion,
    required RegistrarMovimiento registrarMovimiento,
    required SimularDesglose simularDesglose,
    required ObtenerCategorias obtenerCategorias,
    required ObtenerMetas obtenerMetas,
    required ObtenerDeudas obtenerDeudas,
    required ObtenerReporteFinanciero obtenerPresupuesto,
  })  : _registrar = registrarMovimiento,
        _simular = simularDesglose,
        _categorias = obtenerCategorias,
        _metas = obtenerMetas,
        _deudas = obtenerDeudas,
        _presupuesto = obtenerPresupuesto,
        super(RegistroMovimientoState(
          tipo: tipo,
          fecha: DateTime.now(),
          metaId: preseleccion.metaId,
          deudaId: preseleccion.deudaId,
        )) {
    on<RegistroIniciado>(_alIniciar);
    on<RegistroMontoCambiado>(_alCambiarMonto, transformer: debounce(const Duration(milliseconds: 350)));
    on<RegistroDatosCambiados>(_alCambiarDatos);
    on<RegistroEnviado>(_alEnviar);
  }

  final RegistrarMovimiento _registrar;
  final SimularDesglose _simular;
  final ObtenerCategorias _categorias;
  final ObtenerMetas _metas;
  final ObtenerDeudas _deudas;
  final ObtenerReporteFinanciero _presupuesto;

  /// Carga solo lo que necesita el tipo elegido: categorías y presupuesto (gasto), metas (aporte) o deudas (pago).
  Future<void> _alIniciar(RegistroIniciado event, Emitter<RegistroMovimientoState> emit) async {
    switch (state.tipo) {
      case TipoMovimiento.gasto:
        emit(state.copyWith(cargandoOpciones: true));
        final cats = await _categorias(const SinParametros());
        final plan = await _presupuesto(null);
        emit(state.copyWith(
          cargandoOpciones: false,
          categorias: cats.fold((_) => const [], (c) => c),
          presupuestos: plan.fold((_) => const {}, (r) => {for (final l in r.categorias) l.categoriaId: l}),
          error: cats.fold((f) => f.mensaje, (_) => null),
        ));
      case TipoMovimiento.aporteMeta:
        emit(state.copyWith(cargandoOpciones: true));
        final r = await _metas(const SinParametros());
        emit(state.copyWith(
          cargandoOpciones: false,
          metas: r.fold((_) => const [], (m) => m.where((x) => x.estado == 'ACTIVA').toList()),
          error: r.fold((f) => f.mensaje, (_) => null),
        ));
      case TipoMovimiento.pagoDeuda:
        emit(state.copyWith(cargandoOpciones: true));
        final r = await _deudas(const SinParametros());
        emit(state.copyWith(
          cargandoOpciones: false,
          deudas: r.fold((_) => const [], (d) => d.deudas),
          error: r.fold((f) => f.mensaje, (_) => null),
        ));
      default:
        break;
    }
  }

  Future<void> _alCambiarMonto(RegistroMontoCambiado event, Emitter<RegistroMovimientoState> emit) async {
    emit(state.copyWith(monto: event.monto, limpiarDesglose: true));
    if (state.tipo != TipoMovimiento.ingreso || event.monto <= 0) return;
    final r = await _simular(event.monto);
    emit(r.fold((f) => state.copyWith(error: f.mensaje), (d) => state.copyWith(desglose: d)));
  }

  void _alCambiarDatos(RegistroDatosCambiados event, Emitter<RegistroMovimientoState> emit) {
    emit(state.copyWith(
      fecha: event.fecha,
      descripcion: event.descripcion,
      categoriaId: event.categoriaId,
      metaId: event.metaId,
      deudaId: event.deudaId,
      esImprevisto: event.esImprevisto,
    ));
  }

  Future<void> _alEnviar(RegistroEnviado event, Emitter<RegistroMovimientoState> emit) async {
    emit(state.copyWith(enviando: true));
    final descripcion = state.descripcion.trim();
    final r = await _registrar(NuevoMovimiento(
      tipo: state.tipo,
      monto: state.monto,
      fecha: state.fecha,
      descripcion: descripcion.isEmpty ? null : descripcion,
      categoriaId: state.tipo == TipoMovimiento.gasto ? state.categoriaId : null,
      metaId: state.tipo == TipoMovimiento.aporteMeta ? state.metaId : null,
      deudaId: state.tipo == TipoMovimiento.pagoDeuda ? state.deudaId : null,
      esPresupuestado: !state.esImprevisto,
    ));
    emit(r.fold(
      (f) => state.copyWith(error: f.mensaje),
      (m) => state.copyWith(guardado: m),
    ));
  }
}
