import 'package:equatable/equatable.dart';

import '../../../../core/domain/dimension.dart';

enum UnidadHabito {
  minutos('MINUTOS', 'min', 5),
  horas('HORAS', 'h', 0.5),
  veces('VECES', 'veces', 1),
  booleano('BOOLEANO', '', 1),
  vasos('VASOS', 'vasos', 1),
  pasos('PASOS', 'pasos', 500);

  const UnidadHabito(this.api, this.sufijo, this.paso);

  final String api;
  final String sufijo;

  /// Cuánto suma o resta un toque en la pantalla.
  final double paso;

  static UnidadHabito desdeApi(String v) => values.firstWhere((u) => u.api == v);
}

/// Un hábito con lo registrado hoy y en la semana (domingo a sábado).
class HabitoDelDia extends Equatable {
  const HabitoDelDia({
    required this.codigo,
    required this.dimension,
    required this.nombre,
    required this.unidad,
    required this.diaria,
    required this.metaValor,
    required this.valorHoy,
    required this.valorSemana,
    required this.cumplimientoPct,
    this.descripcion,
    this.referenciaBiblica,
  });

  final String codigo;
  final Dimension dimension;
  final String nombre;
  final String? descripcion;
  final UnidadHabito unidad;

  /// La meta es por día (true) o por semana (false).
  final bool diaria;
  final double metaValor;
  final String? referenciaBiblica;
  final double valorHoy;
  final double valorSemana;
  final double cumplimientoPct;

  /// Lo que se compara contra la meta según su frecuencia.
  double get valorActual => diaria ? valorHoy : valorSemana;

  bool get cumplido => cumplimientoPct >= 100;

  @override
  List<Object?> get props => [codigo, dimension, nombre, descripcion, unidad, diaria, metaValor, referenciaBiblica,
        valorHoy, valorSemana, cumplimientoPct];
}
