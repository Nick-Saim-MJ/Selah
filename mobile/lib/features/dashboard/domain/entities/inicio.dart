import 'package:equatable/equatable.dart';

import '../../../../core/domain/semaforo.dart';

/// Todo lo que muestra la pantalla Inicio (spec 4.3): cada tarjeta es un atajo, no edita datos.
class Inicio extends Equatable {
  const Inicio({
    required this.periodo,
    required this.ratioGasto,
    required this.ratioDeuda,
    required this.ratioAhorro,
    required this.ingresos,
    required this.gastos,
    required this.saldoDisponible,
    required this.apartadoTotal,
    required this.diezmoPendiente,
    required this.ofrendaPendiente,
    required this.reflexionVisible,
    required this.reflexionRespondida,
    required this.notificacionesNoLeidas,
    this.semaforo,
    this.categoriaDesvio,
    this.variacionDesvio,
    this.metaPrincipal,
  });

  final String periodo;
  final Semaforo? semaforo;
  final double ratioGasto;
  final double ratioDeuda;
  final double ratioAhorro;
  final double ingresos;
  final double gastos;

  /// Ya sin el diezmo ni la ofrenda apartados.
  final double saldoDisponible;
  final double apartadoTotal;
  final String? categoriaDesvio;
  final double? variacionDesvio;
  final double diezmoPendiente;
  final double ofrendaPendiente;
  final MetaResumen? metaPrincipal;
  final bool reflexionVisible;
  final bool reflexionRespondida;
  final int notificacionesNoLeidas;

  double get ratioTotal => ratioGasto + ratioDeuda;

  @override
  List<Object?> get props => [periodo, semaforo, ratioGasto, ratioDeuda, ratioAhorro, ingresos, gastos,
        saldoDisponible, apartadoTotal, categoriaDesvio, variacionDesvio, diezmoPendiente, ofrendaPendiente,
        metaPrincipal, reflexionVisible, reflexionRespondida, notificacionesNoLeidas];
}

class MetaResumen extends Equatable {
  const MetaResumen({required this.id, required this.nombre, required this.montoActual, required this.montoObjetivo, required this.porcentaje});

  final String id;
  final String nombre;
  final double montoActual;
  final double montoObjetivo;
  final double porcentaje;

  @override
  List<Object?> get props => [id, nombre, montoActual, montoObjetivo, porcentaje];
}
