import 'package:equatable/equatable.dart';

import '../../../../core/domain/dimension.dart';
import '../../../../core/domain/semaforo.dart';

/// Presupuestado vs. real de una categoría de gasto en un mes.
class LineaPresupuesto extends Equatable {
  const LineaPresupuesto({
    required this.categoriaId,
    required this.nombre,
    required this.planeado,
    required this.real,
    this.color,
    this.variacionPct,
  });

  final String categoriaId;
  final String nombre;
  final String? color;
  final double planeado;
  final double real;

  /// (real − planeado) / planeado en %; null si la categoría no tiene presupuesto.
  final double? variacionPct;

  bool get excedida => planeado > 0 && real > planeado;

  /// Lo que queda del presupuesto (negativo si se pasó).
  double get restante => planeado - real;

  @override
  List<Object?> get props => [categoriaId, nombre, color, planeado, real, variacionPct];
}

/// Semáforo, ratios y presupuestado vs. real del mes (spec 4.7).
class ReporteFinanciero extends Equatable {
  const ReporteFinanciero({
    required this.periodo,
    required this.ingresos,
    required this.gastos,
    required this.pagosDeuda,
    required this.aportesMeta,
    required this.saldoDisponible,
    required this.apartadoTotal,
    required this.ratioGasto,
    required this.ratioDeuda,
    required this.ratioAhorro,
    required this.categorias,
    this.semaforo,
    this.principalDesvio,
  });

  final String periodo;
  final double ingresos;
  final double gastos;
  final double pagosDeuda;
  final double aportesMeta;
  final double saldoDisponible;
  final double apartadoTotal;
  final double ratioGasto;
  final double ratioDeuda;
  final double ratioAhorro;

  /// Null si el mes no tiene ingresos: sin ingresos no hay veredicto.
  final Semaforo? semaforo;
  final List<LineaPresupuesto> categorias;
  final LineaPresupuesto? principalDesvio;

  /// Peso de (gastos + deuda) sobre el ingreso: lo que mide el semáforo.
  double get ratioTotal => ratioGasto + ratioDeuda;

  @override
  List<Object?> get props => [periodo, ingresos, gastos, pagosDeuda, aportesMeta, saldoDisponible, apartadoTotal,
        ratioGasto, ratioDeuda, ratioAhorro, semaforo, categorias, principalDesvio];
}

class HabitoCumplimiento extends Equatable {
  const HabitoCumplimiento({required this.codigo, required this.nombre, required this.cumplimientoPct, this.fuente});

  final String codigo;
  final String nombre;
  final double cumplimientoPct;

  /// "APP" o el código del sistema de otro equipo que aportó el dato.
  final String? fuente;

  @override
  List<Object?> get props => [codigo, nombre, cumplimientoPct, fuente];
}

/// Reporte integral de mayordomía: Tiempo, Talento, Tesoro y Templo (0-100 cada uno).
class ReporteMayordomia extends Equatable {
  const ReporteMayordomia({
    required this.periodo,
    required this.puntajes,
    required this.habitos,
    this.global,
    this.semaforo,
    this.diezmoAlDia,
  });

  final String periodo;

  /// Una dimensión sin datos en el periodo no aparece (o es null).
  final Map<Dimension, double?> puntajes;
  final double? global;
  final Semaforo? semaforo;
  final Map<Dimension, List<HabitoCumplimiento>> habitos;

  /// Null si en el mes no hubo diezmo que entregar.
  final bool? diezmoAlDia;

  @override
  List<Object?> get props => [periodo, puntajes, global, semaforo, habitos, diezmoAlDia];
}
