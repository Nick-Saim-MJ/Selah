import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../domain/entities/tipo_movimiento.dart';
import 'tipo_movimiento_ui.dart';

/// Botón `+` → elegir tipo (spec 4.4). El diezmo no aparece: se entrega desde Diezmos.
Future<TipoMovimiento?> mostrarSelectorTipo(BuildContext context) {
  const tipos = [
    TipoMovimiento.ingreso,
    TipoMovimiento.gasto,
    TipoMovimiento.pagoDeuda,
    TipoMovimiento.aporteMeta,
  ];
  return showModalBottomSheet<TipoMovimiento>(
    context: context,
    useRootNavigator: true, // por encima del tab bar
    showDragHandle: true,
    backgroundColor: AppColors.superficie,
    builder: (context) => SafeArea(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          for (final t in tipos)
            ListTile(
              leading: CircleAvatar(
                backgroundColor: t.color.withValues(alpha: 0.14),
                child: Icon(t.icono, color: t.color),
              ),
              title: Text(t.etiqueta),
              onTap: () => Navigator.pop(context, t),
            ),
          const SizedBox(height: 8),
        ],
      ),
    ),
  );
}
