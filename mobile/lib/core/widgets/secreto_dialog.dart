import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../theme/app_colors.dart';
import 'async_view.dart';

/// Muestra un valor que NO se podrá volver a ver (API key, secreto de webhook, contraseña temporal),
/// con botón para copiarlo. El usuario debe confirmar que lo guardó.
Future<void> mostrarSecretoUnaVez(
  BuildContext context, {
  required String titulo,
  required String secreto,
  required String aviso,
}) {
  return showDialog<void>(
    context: context,
    barrierDismissible: false,
    builder: (context) => AlertDialog(
      title: Text(titulo),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(color: AppColors.chip, borderRadius: BorderRadius.circular(12)),
            child: SelectableText(secreto, style: const TextStyle(fontFamily: 'monospace', fontSize: 13)),
          ),
          const SizedBox(height: 12),
          Text(aviso, style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.rojo)),
        ],
      ),
      actions: [
        TextButton.icon(
          onPressed: () async {
            await Clipboard.setData(ClipboardData(text: secreto));
            if (context.mounted) avisar(context, 'Copiado');
          },
          icon: const Icon(Icons.copy),
          label: const Text('Copiar'),
        ),
        FilledButton(
          onPressed: () => Navigator.pop(context),
          style: FilledButton.styleFrom(minimumSize: const Size(120, 44)),
          child: const Text('Ya lo guardé'),
        ),
      ],
    ),
  );
}

/// Contraseña temporal legible y sin caracteres confusos (sin 0/O, 1/l/I).
String generarPasswordTemporal() {
  const letras = 'abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  final r = Random.secure();
  return List.generate(10, (_) => letras[r.nextInt(letras.length)]).join();
}
