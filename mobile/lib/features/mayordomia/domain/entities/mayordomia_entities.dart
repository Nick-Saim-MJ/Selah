import 'package:equatable/equatable.dart';

/// Diezmo y ofrenda apartados en un mes (spec 4.5). Nada de esto forma parte del saldo disponible.
class ResumenMayordomia extends Equatable {
  const ResumenMayordomia({
    required this.periodo,
    required this.diezmoApartado,
    required this.diezmoPendiente,
    required this.ofrendaApartada,
    required this.ofrendaPendiente,
    required this.diezmoAlDia,
  });

  /// YYYY-MM
  final String periodo;
  final double diezmoApartado;
  final double diezmoPendiente;
  final double ofrendaApartada;
  final double ofrendaPendiente;
  final bool diezmoAlDia;

  /// Hubo ingresos ese mes y por tanto diezmo que entregar.
  bool get huboDiezmo => diezmoApartado > 0;

  @override
  List<Object?> get props =>
      [periodo, diezmoApartado, diezmoPendiente, ofrendaApartada, ofrendaPendiente, diezmoAlDia];
}

/// Mes actual + los meses anteriores para ver la constancia.
class DiezmosVista extends Equatable {
  const DiezmosVista({required this.mesActual, required this.historial});

  final ResumenMayordomia mesActual;

  /// Del más antiguo al más reciente (incluye el actual).
  final List<ResumenMayordomia> historial;

  @override
  List<Object?> get props => [mesActual, historial];
}

/// Parámetros de mayordomía del hogar (asistente paso 3 y Configuración).
class ConfigMayordomia extends Equatable {
  const ConfigMayordomia({
    this.pctDiezmo = 10,
    this.ofrendaActiva = true,
    this.pctOfrenda = 2,
    this.diaEntregaDiezmo,
    this.modoSabadoActivo = true,
  });

  final double pctDiezmo;
  final bool ofrendaActiva;
  final double pctOfrenda;
  final int? diaEntregaDiezmo;

  /// Entre el viernes y el sábado al atardecer no se envían avisos de consumo.
  final bool modoSabadoActivo;

  @override
  List<Object?> get props => [pctDiezmo, ofrendaActiva, pctOfrenda, diaEntregaDiezmo, modoSabadoActivo];
}

enum TipoApartado {
  diezmo('DIEZMO', 'Diezmo'),
  ofrenda('OFRENDA', 'Ofrenda');

  const TipoApartado(this.api, this.etiqueta);

  final String api;
  final String etiqueta;
}

class EntregaResultado extends Equatable {
  const EntregaResultado({required this.montoEntregado, required this.apartadosEntregados});

  final double montoEntregado;
  final int apartadosEntregados;

  @override
  List<Object?> get props => [montoEntregado, apartadosEntregados];
}
