import 'package:equatable/equatable.dart';

enum TipoMeta {
  emergencia('EMERGENCIA', 'Fondo de emergencia'),
  especifica('ESPECIFICA', 'Meta específica'),
  libre('LIBRE', 'Ahorro libre');

  const TipoMeta(this.api, this.etiqueta);

  final String api;
  final String etiqueta;

  static TipoMeta desdeApi(String v) => values.firstWhere((t) => t.api == v);
}

/// Meta de ahorro con propósito nombrado (Prov. 6:6-8): no se ahorra "por ahorrar".
class MetaAhorro extends Equatable {
  const MetaAhorro({
    required this.id,
    required this.nombre,
    required this.proposito,
    required this.tipo,
    required this.montoObjetivo,
    this.montoActual = 0,
    this.porcentaje = 0,
    this.restante = 0,
    this.fechaObjetivo,
    this.aporteMensualSugerido,
    this.esPrincipal = false,
    this.estado = 'ACTIVA',
  });

  /// Id vacío = meta nueva.
  final String id;
  final String nombre;
  final String proposito;
  final TipoMeta tipo;
  final double montoObjetivo;
  final double montoActual;
  final double porcentaje;
  final double restante;
  final DateTime? fechaObjetivo;
  final double? aporteMensualSugerido;
  final bool esPrincipal;
  final String estado;

  bool get completada => estado == 'COMPLETADA';

  @override
  List<Object?> get props => [id, nombre, proposito, tipo, montoObjetivo, montoActual, porcentaje, restante,
        fechaObjetivo, aporteMensualSugerido, esPrincipal, estado];
}

/// Deuda con cuota calculada (no digitada) y meses restantes.
class Deuda extends Equatable {
  const Deuda({
    required this.id,
    required this.nombre,
    required this.montoOriginal,
    required this.saldoActual,
    required this.tasaAnual,
    required this.plazoMeses,
    required this.cuotaMensual,
    required this.porcentajePagado,
    this.acreedor,
    this.mesesRestantes,
    this.diaPago,
  });

  final String id;
  final String nombre;
  final String? acreedor;
  final double montoOriginal;
  final double saldoActual;
  final double tasaAnual;
  final int plazoMeses;
  final double cuotaMensual;

  /// Null si la cuota ni siquiera cubre los intereses.
  final int? mesesRestantes;
  final double porcentajePagado;
  final int? diaPago;

  @override
  List<Object?> get props => [id, nombre, acreedor, montoOriginal, saldoActual, tasaAnual, plazoMeses,
        cuotaMensual, mesesRestantes, porcentajePagado, diaPago];
}

/// Deudas activas y la alerta de sobreendeudamiento (cuotas > 40% del ingreso).
class ResumenDeudas extends Equatable {
  const ResumenDeudas({
    required this.deudas,
    required this.cuotaTotalMensual,
    required this.ingresoMensualBase,
    required this.porcentajeCuotasSobreIngreso,
    required this.superaLimite,
    required this.limitePorcentaje,
  });

  final List<Deuda> deudas;
  final double cuotaTotalMensual;
  final double ingresoMensualBase;
  final double porcentajeCuotasSobreIngreso;
  final bool superaLimite;
  final double limitePorcentaje;

  @override
  List<Object?> get props => [deudas, cuotaTotalMensual, ingresoMensualBase, porcentajeCuotasSobreIngreso,
        superaLimite, limitePorcentaje];
}

/// Datos para registrar una deuda; la cuota la calcula el servidor.
class NuevaDeuda extends Equatable {
  const NuevaDeuda({
    required this.nombre,
    required this.montoOriginal,
    required this.tasaAnual,
    required this.plazoMeses,
    this.acreedor,
    this.saldoActual,
    this.diaPago,
  });

  final String nombre;
  final String? acreedor;
  final double montoOriginal;

  /// Si la deuda ya empezó: lo que debes hoy. Por defecto, el monto original.
  final double? saldoActual;
  final double tasaAnual;
  final int plazoMeses;
  final int? diaPago;

  @override
  List<Object?> get props => [nombre, acreedor, montoOriginal, saldoActual, tasaAnual, plazoMeses, diaPago];
}
