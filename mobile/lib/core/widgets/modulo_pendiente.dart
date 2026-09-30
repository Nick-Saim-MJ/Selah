import 'package:flutter/material.dart';

import '../theme/app_colors.dart';

/// Marcador para features cuya estructura existe pero aún no se implementa.
/// Muestra el principio bíblico y lo que hará la pantalla (según la especificación).
class ModuloPendiente extends StatelessWidget {
  const ModuloPendiente({
    super.key,
    required this.principio,
    required this.referencia,
    required this.funciones,
  });

  final String principio;
  final String referencia;
  final List<String> funciones;

  @override
  Widget build(BuildContext context) {
    final texto = Theme.of(context).textTheme;
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Card(
          color: AppColors.terracotaTinte,
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(principio, style: texto.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                const SizedBox(height: 4),
                Text(referencia, style: texto.labelMedium?.copyWith(color: AppColors.terracota)),
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Próximamente', style: texto.labelLarge?.copyWith(color: AppColors.textoSecundario)),
                const SizedBox(height: 8),
                for (final f in funciones)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 4),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Icon(Icons.circle, size: 6, color: AppColors.textoTenue),
                        const SizedBox(width: 10),
                        Expanded(child: Text(f, style: texto.bodyMedium)),
                      ],
                    ),
                  ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
