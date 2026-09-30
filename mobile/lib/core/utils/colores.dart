import 'package:flutter/material.dart';

import '../theme/app_colors.dart';

/// "#RRGGBB" → [Color]. Si falta o es inválido devuelve un gris neutro.
Color colorDeHex(String? hex) {
  if (hex == null || !RegExp(r'^#[0-9A-Fa-f]{6}$').hasMatch(hex)) return AppColors.textoTenue;
  return Color(int.parse('FF${hex.substring(1)}', radix: 16));
}

/// [Color] → "#RRGGBB".
String hexDeColor(Color c) {
  String dos(double canal) => (canal * 255).round().toRadixString(16).padLeft(2, '0');
  return '#${dos(c.r)}${dos(c.g)}${dos(c.b)}'.toUpperCase();
}

/// Paleta que se ofrece al crear una categoría.
const paletaCategorias = <String>[
  '#5E7BAA', '#887B2C', '#258B86', '#9E658B', '#816FA3', '#69625A', '#B27744', '#A96655', '#36884D', '#BC4945',
];
