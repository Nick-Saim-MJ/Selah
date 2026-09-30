import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import 'app_colors.dart';

/// Tema Material 3 con la tipografía Epilogue del prototipo.
abstract final class AppTheme {
  static ThemeData claro() {
    final base = ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(
        seedColor: AppColors.primario,
        primary: AppColors.primario,
        secondary: AppColors.terracota,
        surface: AppColors.superficie,
        error: AppColors.rojo,
      ),
      scaffoldBackgroundColor: AppColors.fondo,
    );

    final texto = GoogleFonts.epilogueTextTheme(base.textTheme).apply(
      bodyColor: AppColors.texto,
      displayColor: AppColors.texto,
    );

    return base.copyWith(
      textTheme: texto,
      appBarTheme: AppBarTheme(
        backgroundColor: AppColors.fondo,
        foregroundColor: AppColors.texto,
        elevation: 0,
        scrolledUnderElevation: 0,
        titleTextStyle: texto.titleLarge?.copyWith(fontWeight: FontWeight.w700),
      ),
      cardTheme: CardThemeData(
        color: AppColors.superficie,
        elevation: 0,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
          side: const BorderSide(color: AppColors.borde),
        ),
      ),
      chipTheme: base.chipTheme.copyWith(
        backgroundColor: AppColors.chip,
        selectedColor: AppColors.primario,
        side: BorderSide.none,
        shape: const StadiumBorder(),
        labelStyle: texto.labelLarge?.copyWith(color: AppColors.textoSecundario),
        secondaryLabelStyle: texto.labelLarge?.copyWith(color: Colors.white),
        showCheckmark: false,
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: AppColors.primario,
          minimumSize: const Size.fromHeight(52),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          textStyle: texto.titleMedium?.copyWith(fontWeight: FontWeight.w700),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.superficie,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: AppColors.borde),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: AppColors.borde),
        ),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: AppColors.superficie,
        indicatorColor: AppColors.chip,
        labelTextStyle: WidgetStatePropertyAll(
          texto.labelSmall?.copyWith(fontSize: 11, fontWeight: FontWeight.w600, letterSpacing: 0),
        ),
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: AppColors.primario,
        foregroundColor: Colors.white,
      ),
    );
  }
}
