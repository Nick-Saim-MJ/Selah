import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/formato.dart';
import '../../domain/entities/movimiento.dart';
import 'tipo_movimiento_ui.dart';

class MovimientoTile extends StatelessWidget {
  const MovimientoTile({super.key, required this.movimiento, this.onTap});

  final Movimiento movimiento;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final texto = Theme.of(context).textTheme;
    final m = movimiento;
    final signo = m.tipo.esEntrada ? '+' : '−';
    return ListTile(
      onTap: onTap,
      contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 2),
      leading: CircleAvatar(
        backgroundColor: m.tipo.color.withValues(alpha: 0.14),
        child: Icon(m.tipo.icono, color: m.tipo.color, size: 20),
      ),
      title: Text(m.descripcion ?? m.tipo.etiqueta, style: texto.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
      subtitle: Text(m.tipo.etiqueta, style: texto.bodySmall?.copyWith(color: AppColors.textoTenue)),
      trailing: Text(
        '$signo${Formato.moneda(m.monto)}',
        style: texto.titleSmall?.copyWith(
          fontWeight: FontWeight.w700,
          color: m.tipo.esEntrada ? AppColors.ingreso : AppColors.texto,
        ),
      ),
    );
  }
}
