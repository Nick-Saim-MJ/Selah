import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../domain/entities/tipo_movimiento.dart';

/// Colores e íconos por tipo (preocupación de presentación, no de dominio).
extension TipoMovimientoUi on TipoMovimiento {
  Color get color => switch (this) {
        TipoMovimiento.ingreso => AppColors.ingreso,
        TipoMovimiento.gasto => AppColors.gasto,
        TipoMovimiento.diezmo || TipoMovimiento.ofrenda => AppColors.terracota,
        TipoMovimiento.pagoDeuda => AppColors.pagoDeuda,
        TipoMovimiento.aporteMeta => AppColors.aporteMeta,
      };

  IconData get icono => switch (this) {
        TipoMovimiento.ingreso => Icons.south_west_rounded,
        TipoMovimiento.gasto => Icons.shopping_bag_outlined,
        TipoMovimiento.diezmo => Icons.volunteer_activism_outlined,
        TipoMovimiento.ofrenda => Icons.favorite_border_rounded,
        TipoMovimiento.pagoDeuda => Icons.credit_card_outlined,
        TipoMovimiento.aporteMeta => Icons.savings_outlined,
      };

  String get tituloFormulario => switch (this) {
        TipoMovimiento.ingreso => 'Nuevo ingreso',
        TipoMovimiento.gasto => 'Nuevo gasto',
        TipoMovimiento.ofrenda => 'Ofrenda específica',
        TipoMovimiento.pagoDeuda => 'Pago de deuda',
        TipoMovimiento.aporteMeta => 'Aporte a meta',
        TipoMovimiento.diezmo => 'Diezmo',
      };
}
