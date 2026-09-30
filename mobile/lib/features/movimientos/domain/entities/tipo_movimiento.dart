/// Mismo catálogo que el backend (movimiento.tipo).
enum TipoMovimiento {
  ingreso('INGRESO', 'Ingreso'),
  gasto('GASTO', 'Gasto'),
  diezmo('DIEZMO', 'Diezmo'),
  ofrenda('OFRENDA', 'Ofrenda'),
  pagoDeuda('PAGO_DEUDA', 'Pago de deuda'),
  aporteMeta('APORTE_META', 'Aporte a meta');

  const TipoMovimiento(this.api, this.etiqueta);

  final String api;
  final String etiqueta;

  bool get esEntrada => this == TipoMovimiento.ingreso;

  static TipoMovimiento desdeApi(String valor) => values.firstWhere((t) => t.api == valor);
}
