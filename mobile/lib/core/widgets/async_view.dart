import 'package:flutter/material.dart';

import '../bloc/async_state.dart';
import '../theme/app_colors.dart';

/// Pinta los tres casos de una pantalla que carga datos: cargando, error con "Reintentar" y contenido.
class AsyncView<T> extends StatelessWidget {
  const AsyncView({
    super.key,
    required this.state,
    required this.onRetry,
    required this.builder,
    this.onRefresh,
  });

  final AsyncState<T> state;
  final VoidCallback onRetry;
  final Widget Function(BuildContext context, T datos) builder;

  /// Si se indica, el contenido soporta "tirar para refrescar".
  final Future<void> Function()? onRefresh;

  @override
  Widget build(BuildContext context) {
    final datos = state.datos;
    if (datos == null) {
      if (state.estado == Estado.fallo) {
        return MensajeCentrado(
          icono: Icons.cloud_off_outlined,
          texto: state.error ?? 'No se pudo cargar',
          accion: 'Reintentar',
          onAccion: onRetry,
        );
      }
      return const Center(child: CircularProgressIndicator());
    }
    final contenido = builder(context, datos);
    if (onRefresh == null) return contenido;
    return RefreshIndicator(onRefresh: onRefresh!, child: contenido);
  }
}

class MensajeCentrado extends StatelessWidget {
  const MensajeCentrado({super.key, required this.texto, this.icono, this.accion, this.onAccion});

  final String texto;
  final IconData? icono;
  final String? accion;
  final VoidCallback? onAccion;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (icono != null) Icon(icono, size: 44, color: AppColors.textoTenue),
            if (icono != null) const SizedBox(height: 12),
            Text(texto, textAlign: TextAlign.center, style: Theme.of(context).textTheme.bodyLarge),
            if (accion != null) ...[
              const SizedBox(height: 8),
              TextButton(onPressed: onAccion, child: Text(accion!)),
            ],
          ],
        ),
      ),
    );
  }
}

/// SnackBar breve para confirmar o avisar de un error.
void avisar(BuildContext context, String mensaje, {bool error = false}) {
  ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(SnackBar(
      content: Text(mensaje),
      backgroundColor: error ? AppColors.rojo : null,
    ));
}
