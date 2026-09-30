import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/domain/dimension.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../domain/entities/habitos_entities.dart';
import '../cubit/habitos_cubit.dart';

/// Mis hábitos de hoy: Tiempo, Talento y Templo. Alimentan el reporte de mayordomía integral.
class HabitosPage extends StatelessWidget {
  const HabitosPage({super.key});

  static const _orden = [Dimension.tiempo, Dimension.talento, Dimension.templo, Dimension.tesoro];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mis hábitos de hoy')),
      body: BlocBuilder<HabitosCubit, AsyncState<List<HabitoDelDia>>>(
        builder: (context, state) => AsyncView<List<HabitoDelDia>>(
          state: state,
          onRetry: context.read<HabitosCubit>().cargar,
          onRefresh: context.read<HabitosCubit>().cargar,
          builder: (context, habitos) => ListView(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 32),
            children: [
              for (final d in _orden)
                if (habitos.any((h) => h.dimension == d)) ...[
                  TituloSeccion(d.etiqueta),
                  Text(d.descripcion,
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.textoSecundario)),
                  const SizedBox(height: 8),
                  for (final h in habitos.where((h) => h.dimension == d)) ...[
                    _FilaHabito(habito: h),
                    const SizedBox(height: 8),
                  ],
                ],
            ],
          ),
        ),
      ),
    );
  }
}

class _FilaHabito extends StatelessWidget {
  const _FilaHabito({required this.habito});

  final HabitoDelDia habito;

  Future<void> _guardar(BuildContext context, double valor) async {
    final error = await context.read<HabitosCubit>().registrar(habito.codigo, valor < 0 ? 0 : valor);
    if (context.mounted && error != null) avisar(context, error, error: true);
  }

  String _formato(double v) => v == v.roundToDouble() ? v.toStringAsFixed(0) : v.toStringAsFixed(1);

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final h = habito;
    final meta = h.unidad == UnidadHabito.booleano
        ? (h.diaria ? 'Cada día' : 'Cada semana')
        : 'Meta: ${_formato(h.metaValor)} ${h.unidad.sufijo} ${h.diaria ? 'al día' : 'a la semana'}';
    return Tarjeta(
      padding: const EdgeInsets.fromLTRB(16, 12, 12, 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(h.nombre, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
                    Text(meta, style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
                  ],
                ),
              ),
              if (h.unidad == UnidadHabito.booleano)
                Switch(value: h.valorHoy >= 1, onChanged: (v) => _guardar(context, v ? 1 : 0))
              else
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    IconButton(
                      tooltip: 'Restar',
                      icon: const Icon(Icons.remove_circle_outline),
                      onPressed: h.valorHoy <= 0 ? null : () => _guardar(context, h.valorHoy - h.unidad.paso),
                    ),
                    SizedBox(
                      width: 52,
                      child: Text('${_formato(h.valorHoy)}\n${h.unidad.sufijo}',
                          textAlign: TextAlign.center, style: t.labelMedium?.copyWith(fontWeight: FontWeight.w700)),
                    ),
                    IconButton(
                      tooltip: 'Sumar',
                      icon: const Icon(Icons.add_circle_outline),
                      onPressed: () => _guardar(context, h.valorHoy + h.unidad.paso),
                    ),
                  ],
                ),
            ],
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              Expanded(child: BarraProgreso(valor: h.cumplimientoPct / 100, color: h.cumplido ? AppColors.verde : AppColors.primario, alto: 6)),
              const SizedBox(width: 10),
              Text('${h.cumplimientoPct.round()}%', style: t.labelMedium?.copyWith(fontWeight: FontWeight.w700)),
            ],
          ),
          if (!h.diaria)
            Padding(
              padding: const EdgeInsets.only(top: 4),
              child: Text('Esta semana: ${_formato(h.valorSemana)} ${h.unidad.sufijo}',
                  style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
            ),
          if (h.referenciaBiblica != null)
            Padding(
              padding: const EdgeInsets.only(top: 2),
              child: Text(h.referenciaBiblica!, style: t.labelSmall?.copyWith(color: AppColors.terracota)),
            ),
        ],
      ),
    );
  }
}
