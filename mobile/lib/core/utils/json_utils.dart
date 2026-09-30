/// Conversiones defensivas para JSON del backend (los montos llegan como número; los nulos se omiten).
double dbl(Object? v) => (v as num).toDouble();

double? dblN(Object? v) => v == null ? null : (v as num).toDouble();

DateTime? fechaN(Object? v) => v == null ? null : DateTime.parse(v as String);

/// Lista de mapas JSON tipada.
List<Map<String, dynamic>> lista(Object? v) => (v as List).cast<Map<String, dynamic>>();
