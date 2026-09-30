import 'package:flutter/material.dart';

import '../domain/semaforo.dart';
import '../theme/app_colors.dart';

/// Tarjeta con título opcional; base visual de casi todas las pantallas.
class Tarjeta extends StatelessWidget {
  const Tarjeta({super.key, required this.child, this.color, this.onTap, this.padding = const EdgeInsets.all(18)});

  final Widget child;
  final Color? color;
  final VoidCallback? onTap;
  final EdgeInsetsGeometry padding;

  @override
  Widget build(BuildContext context) {
    return Card(
      color: color,
      clipBehavior: Clip.antiAlias,
      child: InkWell(onTap: onTap, child: Padding(padding: padding, child: child)),
    );
  }
}

class TituloSeccion extends StatelessWidget {
  const TituloSeccion(this.texto, {super.key, this.accion});

  final String texto;
  final Widget? accion;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(4, 20, 4, 8),
      child: Row(
        children: [
          Expanded(
            child: Text(texto,
                style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
          ),
          ?accion,
        ],
      ),
    );
  }
}

/// Barra de progreso redondeada (0-1).
class BarraProgreso extends StatelessWidget {
  const BarraProgreso({super.key, required this.valor, this.color = AppColors.primario, this.alto = 8});

  final double valor;
  final Color color;
  final double alto;

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(alto),
      child: LinearProgressIndicator(
        value: valor.clamp(0.0, 1.0),
        minHeight: alto,
        color: color,
        backgroundColor: AppColors.chip,
      ),
    );
  }
}

/// Colores y textos del semáforo (la enumeración vive en el dominio: core/domain/semaforo.dart).
extension SemaforoUi on Semaforo {
  Color get color => switch (this) {
        Semaforo.verde => AppColors.verde,
        Semaforo.amarillo => AppColors.ambar,
        Semaforo.rojo => AppColors.rojo,
      };

  Color get tinte => switch (this) {
        Semaforo.verde => AppColors.verdeTinte,
        Semaforo.amarillo => AppColors.ambarTinte,
        Semaforo.rojo => AppColors.rojoTinte,
      };

  String get etiqueta => switch (this) {
        Semaforo.verde => 'Verde',
        Semaforo.amarillo => 'Amarillo',
        Semaforo.rojo => 'Rojo',
      };
}

class PuntoSemaforo extends StatelessWidget {
  const PuntoSemaforo(this.semaforo, {super.key, this.tamano = 14});

  final Semaforo? semaforo;
  final double tamano;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: tamano,
      height: tamano,
      decoration: BoxDecoration(color: semaforo?.color ?? AppColors.borde, shape: BoxShape.circle),
    );
  }
}

/// Fila "etiqueta ........ valor".
class FilaDato extends StatelessWidget {
  const FilaDato(this.etiqueta, this.valor, {super.key, this.destacado = false, this.colorValor});

  final String etiqueta;
  final String valor;
  final bool destacado;
  final Color? colorValor;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final estilo = destacado ? t.titleSmall : t.bodyMedium;
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Flexible(child: Text(etiqueta, style: estilo?.copyWith(color: destacado ? null : AppColors.textoSecundario))),
          const SizedBox(width: 12),
          Text(valor, style: estilo?.copyWith(fontWeight: FontWeight.w700, color: colorValor)),
        ],
      ),
    );
  }
}

/// Chip pequeño de estado (activo, bloqueado, principal…).
class Etiqueta extends StatelessWidget {
  const Etiqueta(this.texto, {super.key, this.color = AppColors.primario});

  final String texto;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
      decoration: BoxDecoration(color: color.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(20)),
      child: Text(texto,
          style: Theme.of(context).textTheme.labelSmall?.copyWith(color: color, fontWeight: FontWeight.w700)),
    );
  }
}

/// Confirmación antes de una acción delicada. Devuelve true si el usuario acepta.
Future<bool> confirmar(BuildContext context, {required String titulo, required String mensaje, String aceptar = 'Aceptar'}) async {
  final r = await showDialog<bool>(
    context: context,
    builder: (context) => AlertDialog(
      title: Text(titulo),
      content: Text(mensaje),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Cancelar')),
        FilledButton(
          onPressed: () => Navigator.pop(context, true),
          style: FilledButton.styleFrom(minimumSize: const Size(96, 44)),
          child: Text(aceptar),
        ),
      ],
    ),
  );
  return r ?? false;
}
