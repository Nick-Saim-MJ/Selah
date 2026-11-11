import 'package:flutter/material.dart';

import '../theme/app_colors.dart';

/// Símbolo de SelahFinance (assets/icons/logo_simbolo.png, fondo transparente).
class LogoSimbolo extends StatelessWidget {
  const LogoSimbolo({super.key, this.alto = 32});

  final double alto;

  @override
  Widget build(BuildContext context) => Image.asset(
        'assets/icons/logo_simbolo.png',
        height: alto,
        semanticLabel: 'Logo de SelahFinance',
        filterQuality: FilterQuality.medium,
      );
}

/// Símbolo + nombre en una fila, para cabeceras (AppBar y pantallas de bienvenida).
class LogoMarca extends StatelessWidget {
  const LogoMarca({super.key, this.sufijo, this.alto = 30});

  /// Texto extra tras el nombre, p. ej. «· Admin».
  final String? sufijo;
  final double alto;

  @override
  Widget build(BuildContext context) {
    final estilo = Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800, color: AppColors.primario);
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        LogoSimbolo(alto: alto),
        const SizedBox(width: 8),
        Flexible(child: Text(sufijo == null ? 'SelahFinance' : 'SelahFinance $sufijo', style: estilo, overflow: TextOverflow.ellipsis)),
      ],
    );
  }
}
