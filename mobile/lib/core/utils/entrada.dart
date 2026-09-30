import 'package:flutter/services.dart';

/// Texto de un campo de monto → número. Acepta coma o punto; vacío = 0; inválido = null.
double? parseMonto(String texto) {
  final limpio = texto.trim().replaceAll(',', '.');
  if (limpio.isEmpty) return 0;
  return double.tryParse(limpio);
}

/// Solo dígitos con hasta 2 decimales (punto o coma).
final formatoMonto = FilteringTextInputFormatter.allow(RegExp(r'^\d*[.,]?\d{0,2}'));

/// Formato de un monto para llenar un campo de texto: 900 → "900", 12.5 → "12.50".
String montoParaCampo(double valor) => valor == valor.roundToDouble() ? valor.toStringAsFixed(0) : valor.toStringAsFixed(2);
