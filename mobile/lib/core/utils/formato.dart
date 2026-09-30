import 'package:intl/intl.dart';

/// Formatos de presentación (moneda y fechas) en español.
abstract final class Formato {
  // Convención peruana: "S/ 1,234.56" (el locale es_PE de intl pondría el símbolo al final).
  static final _moneda = NumberFormat.currency(locale: 'en_US', symbol: 'S/ ', decimalDigits: 2);
  static final _monedaEntera = NumberFormat.currency(locale: 'en_US', symbol: 'S/ ', decimalDigits: 0);
  static final _fechaCorta = DateFormat('d MMM', 'es');
  static final _fechaLarga = DateFormat("EEEE d 'de' MMMM", 'es');
  static final _fechaApi = DateFormat('yyyy-MM-dd');
  static final _mesCorto = DateFormat('MMM', 'es');
  static final _mesLargo = DateFormat('MMMM yyyy', 'es');

  static String moneda(num monto) => _moneda.format(monto);

  /// Sin decimales, para cifras grandes de tarjetas: "S/ 3,960".
  static String monedaEntera(num monto) => _monedaEntera.format(monto);

  static String fechaCorta(DateTime fecha) => _fechaCorta.format(fecha);

  static String fechaLarga(DateTime fecha) => _fechaLarga.format(fecha);

  static String fechaApi(DateTime fecha) => _fechaApi.format(fecha);

  /// "2026-09" (formato de periodo del API).
  static String periodo(DateTime fecha) => '${fecha.year.toString().padLeft(4, '0')}-${fecha.month.toString().padLeft(2, '0')}';

  /// "sep" a partir de "2026-09".
  static String mesCorto(String periodo) => _mesCorto.format(DateTime.parse('$periodo-01')).replaceAll('.', '');

  /// "septiembre 2026" a partir de "2026-09".
  static String mesLargo(String periodo) => _mesLargo.format(DateTime.parse('$periodo-01'));

  static String porcentaje(num valor) => '${valor.toStringAsFixed(valor == valor.roundToDouble() ? 0 : 1)}%';
}
