import 'package:flutter/material.dart';

/// Paleta tomada del prototipo HTML (valores OKLCH convertidos a sRGB).
/// Azul petróleo = confianza/orden · Crema cálido = calma · Terracota = mayordomía.
abstract final class AppColors {
  static const primario = Color(0xFF0B3B4A);
  static const primarioOscuro = Color(0xFF002F3E);
  static const fondo = Color(0xFFF6F1EB);
  static const superficie = Color(0xFFFEFBF9);
  static const texto = Color(0xFF1C140C);
  static const textoSecundario = Color(0xFF5C5752);
  static const textoTenue = Color(0xFF847F7A);
  static const borde = Color(0xFFDCD6D1);
  static const chip = Color(0xFFEBE7E2);

  // Mayordomía (diezmos y ofrendas)
  static const terracota = Color(0xFFA96655);
  static const terracotaTinte = Color(0xFFF1D8D1);
  static const terracotaOscuro = Color(0xFF472218);

  // Tipos de movimiento
  static const ingreso = Color(0xFF36884D);
  static const gasto = Color(0xFF332D26);
  static const pagoDeuda = Color(0xFF4D5660);
  static const aporteMeta = primario;

  // Semáforo
  static const verde = Color(0xFF2D8949);
  static const verdeTinte = Color(0xFFD8EFDC);
  static const ambar = Color(0xFFDE9C31);
  static const ambarTinte = Color(0xFFFBE8CE);
  static const rojo = Color(0xFFBC4945);
  static const rojoTinte = Color(0xFFFFDFDC);

  // Categorías sugeridas
  static const categorias = <String, Color>{
    'Vivienda': Color(0xFF5E7BAA),
    'Alimentación': Color(0xFF887B2C),
    'Transporte': Color(0xFF258B86),
    'Salud': Color(0xFF9E658B),
    'Educación': Color(0xFF816FA3),
    'Deudas': Color(0xFF69625A),
    'Ocio': Color(0xFFB27744),
  };
}
