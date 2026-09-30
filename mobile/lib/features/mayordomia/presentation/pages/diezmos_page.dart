import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/router/rutas.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/formato.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../../movimientos/domain/entities/tipo_movimiento.dart';
import '../../domain/entities/mayordomia_entities.dart';
import '../cubit/mayordomia_cubits.dart';

/// Diezmos y ofrendas: lo ya apartado vs. lo disponible (spec 4.5). El diezmo es lo primero, no el sobrante.
class DiezmosPage extends StatelessWidget {
  const DiezmosPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Diezmos y ofrendas')),
      body: BlocBuilder<DiezmosCubit, AsyncState<DiezmosVista>>(
        builder: (context, state) => AsyncView<DiezmosVista>(
          state: state,
          onRetry: context.read<DiezmosCubit>().cargar,
          onRefresh: context.read<DiezmosCubit>().cargar,
          builder: (context, vista) => _Contenido(vista: vista),
        ),
      ),
    );
  }
}

class _Contenido extends StatelessWidget {
  const _Contenido({required this.vista});

  final DiezmosVista vista;

  Future<void> _entregar(BuildContext context, TipoApartado tipo, double monto) async {
    final cubit = context.read<DiezmosCubit>();
    final si = await confirmar(
      context,
      titulo: '¿Entregaste el ${tipo.etiqueta.toLowerCase()}?',
      mensaje: 'Se registrará la entrega de ${Formato.moneda(monto)}. Tu saldo disponible no cambia: ese dinero ya estaba apartado.',
      aceptar: 'Sí, lo entregué',
    );
    if (!si || !context.mounted) return;
    final error = await cubit.entregar(tipo);
    if (!context.mounted) return;
    avisar(context, error ?? '¡${tipo.etiqueta} entregado! Gracias por ser fiel.', error: error != null);
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final m = vista.mesActual;
    final entregado = m.huboDiezmo && m.diezmoAlDia;
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Tarjeta(
          color: AppColors.terracotaTinte,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Diezmo de ${Formato.mesLargo(m.periodo)}',
                  style: t.labelLarge?.copyWith(color: AppColors.terracotaOscuro)),
              const SizedBox(height: 4),
              Text(Formato.moneda(m.diezmoApartado),
                  style: t.displaySmall?.copyWith(fontWeight: FontWeight.w800, color: AppColors.terracotaOscuro)),
              const SizedBox(height: 4),
              Text(
                m.huboDiezmo
                    ? 'Apartado de tus ingresos del mes. Ya no forma parte de tu saldo disponible.'
                    : 'Todavía no registraste ingresos este mes.',
                style: t.bodySmall?.copyWith(color: AppColors.terracotaOscuro),
              ),
              const SizedBox(height: 16),
              if (!m.huboDiezmo)
                const SizedBox.shrink()
              else if (entregado)
                const Row(children: [
                  Icon(Icons.check_circle, color: AppColors.verde),
                  SizedBox(width: 8),
                  Text('Diezmo entregado'),
                ])
              else
                FilledButton(
                  onPressed: () => _entregar(context, TipoApartado.diezmo, m.diezmoPendiente),
                  style: FilledButton.styleFrom(backgroundColor: AppColors.terracota),
                  child: Text('Marcar como entregado · ${Formato.moneda(m.diezmoPendiente)}'),
                ),
            ],
          ),
        ),
        if (m.ofrendaApartada > 0) ...[
          const SizedBox(height: 12),
          Tarjeta(
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Ofrenda del mes', style: t.labelLarge?.copyWith(color: AppColors.textoSecundario)),
                      Text(Formato.moneda(m.ofrendaApartada), style: t.titleLarge?.copyWith(fontWeight: FontWeight.w700)),
                    ],
                  ),
                ),
                if (m.ofrendaPendiente > 0)
                  OutlinedButton(
                    onPressed: () => _entregar(context, TipoApartado.ofrenda, m.ofrendaPendiente),
                    child: const Text('Entregada'),
                  )
                else
                  const Icon(Icons.check_circle, color: AppColors.verde),
              ],
            ),
          ),
        ],
        const TituloSeccion('Tu constancia'),
        Tarjeta(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [for (final h in vista.historial) _MesFidelidad(resumen: h)],
              ),
              const SizedBox(height: 12),
              Text('Cada círculo lleno es un mes con el diezmo entregado.',
                  style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
            ],
          ),
        ),
        const TituloSeccion('Ofrendas específicas'),
        Tarjeta(
          onTap: () => context.push(Rutas.nuevoMovimiento(TipoMovimiento.ofrenda)),
          child: Row(
            children: [
              const Icon(Icons.favorite_border_rounded, color: AppColors.terracota),
              const SizedBox(width: 14),
              Expanded(
                child: Text('Registrar una ofrenda para un proyecto, misión u obra social',
                    style: t.bodyMedium),
              ),
              const Icon(Icons.chevron_right),
            ],
          ),
        ),
        const SizedBox(height: 24),
        Center(
          child: Text('Honra a Jehová con tus bienes, y con las primicias de todos tus frutos.\nProverbios 3:9',
              textAlign: TextAlign.center,
              style: t.bodySmall?.copyWith(color: AppColors.terracota, fontStyle: FontStyle.italic)),
        ),
      ],
    );
  }
}

class _MesFidelidad extends StatelessWidget {
  const _MesFidelidad({required this.resumen});

  final ResumenMayordomia resumen;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final lleno = resumen.huboDiezmo && resumen.diezmoAlDia;
    final pendiente = resumen.huboDiezmo && !resumen.diezmoAlDia;
    return Column(
      children: [
        Container(
          width: 34,
          height: 34,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: lleno ? AppColors.terracota : Colors.transparent,
            border: lleno ? null : Border.all(color: pendiente ? AppColors.terracota : AppColors.borde, width: 2),
          ),
          child: lleno ? const Icon(Icons.check, color: Colors.white, size: 18) : null,
        ),
        const SizedBox(height: 6),
        Text(Formato.mesCorto(resumen.periodo), style: t.labelSmall?.copyWith(color: AppColors.textoSecundario)),
      ],
    );
  }
}
