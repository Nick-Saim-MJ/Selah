import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/formato.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../domain/reflexion_domain.dart';
import '../cubit/reflexion_cubit.dart';

/// Reflexión semanal: el único momento de la app pensado para NO actuar, solo revisar (Lev. 23:3).
class ReflexionPage extends StatelessWidget {
  const ReflexionPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Reflexión de la semana')),
      body: BlocBuilder<ReflexionCubit, AsyncState<TarjetaReflexion>>(
        builder: (context, state) => AsyncView<TarjetaReflexion>(
          state: state,
          onRetry: context.read<ReflexionCubit>().cargar,
          builder: (context, tarjeta) => _Contenido(tarjeta: tarjeta),
        ),
      ),
    );
  }
}

class _Contenido extends StatefulWidget {
  const _Contenido({required this.tarjeta});

  final TarjetaReflexion tarjeta;

  @override
  State<_Contenido> createState() => _ContenidoState();
}

class _ContenidoState extends State<_Contenido> {
  late final _respuesta = TextEditingController(text: widget.tarjeta.respuesta ?? '');
  bool _guardando = false;

  @override
  void dispose() {
    _respuesta.dispose();
    super.dispose();
  }

  Future<void> _guardar() async {
    setState(() => _guardando = true);
    final error = await context.read<ReflexionCubit>().responder(_respuesta.text);
    if (!mounted) return;
    setState(() => _guardando = false);
    avisar(context, error ?? 'Guardado. Descansa en paz.', error: error != null);
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final r = widget.tarjeta;
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Text('Tu semana', style: t.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
        const SizedBox(height: 8),
        Tarjeta(
          child: Column(
            children: [
              FilaDato('Entró', Formato.moneda(r.entro), colorValor: AppColors.ingreso),
              FilaDato('Gastaste', Formato.moneda(r.gasto)),
              FilaDato(
                'Diezmo del mes',
                r.diezmoAlDia == null ? 'Sin diezmo aún' : (r.diezmoAlDia! ? 'Entregado' : 'Pendiente'),
                colorValor: r.diezmoAlDia == true ? AppColors.verde : (r.diezmoAlDia == false ? AppColors.ambar : null),
              ),
            ],
          ),
        ),
        const SizedBox(height: 20),
        Tarjeta(
          color: AppColors.primario,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(r.pregunta.texto, style: t.titleLarge?.copyWith(color: Colors.white, fontWeight: FontWeight.w700)),
              if (r.pregunta.referenciaBiblica != null) ...[
                const SizedBox(height: 8),
                Text(r.pregunta.referenciaBiblica!, style: t.labelMedium?.copyWith(color: Colors.white70)),
              ],
            ],
          ),
        ),
        const SizedBox(height: 16),
        TextField(
          controller: _respuesta,
          minLines: 4,
          maxLines: 8,
          maxLength: 2000,
          textCapitalization: TextCapitalization.sentences,
          decoration: const InputDecoration(
            labelText: 'Tu reflexión (opcional)',
            alignLabelWithHint: true,
            helperText: 'Nadie más la ve. No estás obligado a responder.',
          ),
        ),
        const SizedBox(height: 12),
        FilledButton(
          onPressed: _guardando ? null : _guardar,
          child: _guardando
              ? const SizedBox.square(dimension: 22, child: CircularProgressIndicator(strokeWidth: 2.5))
              : const Text('Guardar mi reflexión'),
        ),
        const SizedBox(height: 24),
        Center(
          child: Text('Acuérdate del día de reposo para santificarlo.\nÉxodo 20:8',
              textAlign: TextAlign.center,
              style: t.bodySmall?.copyWith(color: AppColors.terracota, fontStyle: FontStyle.italic)),
        ),
      ],
    );
  }
}
