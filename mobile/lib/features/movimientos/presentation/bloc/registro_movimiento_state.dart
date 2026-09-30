part of 'registro_movimiento_bloc.dart';

final class RegistroMovimientoState extends Equatable {
  const RegistroMovimientoState({
    required this.tipo,
    required this.fecha,
    this.monto = 0,
    this.descripcion = '',
    this.desglose,
    this.categorias = const [],
    this.presupuestos = const {},
    this.metas = const [],
    this.deudas = const [],
    this.categoriaId,
    this.metaId,
    this.deudaId,
    this.esImprevisto = false,
    this.cargandoOpciones = false,
    this.enviando = false,
    this.error,
    this.guardado,
  });

  final TipoMovimiento tipo;
  final DateTime fecha;
  final double monto;
  final String descripcion;

  /// Solo para ingresos: diezmo/ofrenda calculados por el backend.
  final DesgloseIngreso? desglose;

  final List<CategoriaGasto> categorias;

  /// Plan y gasto real del mes por categoría, para el aviso "te quedan S/ X".
  final Map<String, LineaPresupuesto> presupuestos;
  final List<MetaAhorro> metas;
  final List<Deuda> deudas;
  final String? categoriaId;
  final String? metaId;
  final String? deudaId;

  /// Gasto no presupuestado (spec 4.4: ¿presupuestado o imprevisto?).
  final bool esImprevisto;
  final bool cargandoOpciones;
  final bool enviando;
  final String? error;

  /// Distinto de null cuando se guardó con éxito (la página hace pop).
  final Movimiento? guardado;

  /// ¿Ya eligió lo que este tipo exige (categoría, meta o deuda)?
  bool get seleccionCompleta => switch (tipo) {
        TipoMovimiento.gasto => categoriaId != null,
        TipoMovimiento.aporteMeta => metaId != null,
        TipoMovimiento.pagoDeuda => deudaId != null,
        _ => true,
      };

  bool get puedeEnviar => monto > 0 && seleccionCompleta && !enviando;

  LineaPresupuesto? get presupuestoElegido => categoriaId == null ? null : presupuestos[categoriaId];

  /// Cada llamada parte del estado actual: el error y `guardado` se limpian salvo que se indiquen.
  RegistroMovimientoState copyWith({
    DateTime? fecha,
    double? monto,
    String? descripcion,
    DesgloseIngreso? desglose,
    bool limpiarDesglose = false,
    List<CategoriaGasto>? categorias,
    Map<String, LineaPresupuesto>? presupuestos,
    List<MetaAhorro>? metas,
    List<Deuda>? deudas,
    String? categoriaId,
    String? metaId,
    String? deudaId,
    bool? esImprevisto,
    bool? cargandoOpciones,
    bool? enviando,
    String? error,
    Movimiento? guardado,
  }) =>
      RegistroMovimientoState(
        tipo: tipo,
        fecha: fecha ?? this.fecha,
        monto: monto ?? this.monto,
        descripcion: descripcion ?? this.descripcion,
        desglose: limpiarDesglose ? null : (desglose ?? this.desglose),
        categorias: categorias ?? this.categorias,
        presupuestos: presupuestos ?? this.presupuestos,
        metas: metas ?? this.metas,
        deudas: deudas ?? this.deudas,
        categoriaId: categoriaId ?? this.categoriaId,
        metaId: metaId ?? this.metaId,
        deudaId: deudaId ?? this.deudaId,
        esImprevisto: esImprevisto ?? this.esImprevisto,
        cargandoOpciones: cargandoOpciones ?? this.cargandoOpciones,
        enviando: enviando ?? false,
        error: error,
        guardado: guardado,
      );

  @override
  List<Object?> get props => [tipo, fecha, monto, descripcion, desglose, categorias, presupuestos, metas, deudas,
        categoriaId, metaId, deudaId, esImprevisto, cargandoOpciones, enviando, error, guardado];
}
