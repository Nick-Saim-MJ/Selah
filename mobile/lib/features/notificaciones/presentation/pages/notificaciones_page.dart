import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/router/rutas.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/formato.dart';
import '../../../../core/widgets/async_view.dart';
import '../../domain/notificaciones_domain.dart';
import '../cubit/notificaciones_cubits.dart';

/// Bandeja de avisos dentro de la app. Los de consumo se posponen mientras dure el Modo Sábado.
class NotificacionesPage extends StatelessWidget {
  const NotificacionesPage({super.key});

  static const _pestanas = {Rutas.inicio, Rutas.movimientos, Rutas.diezmos, Rutas.metas, Rutas.reportes};

  IconData _icono(Notificacion n) => switch (n.tipo) {
        'PRESUPUESTO_EXCEDIDO' => Icons.warning_amber_rounded,
        'CUOTA_DEUDA' => Icons.credit_card_outlined,
        'DIEZMO_PENDIENTE' => Icons.volunteer_activism_outlined,
        'REFLEXION_SABADO' => Icons.wb_twilight_outlined,
        'RESUMEN_SEMANAL' => Icons.insights_outlined,
        _ => Icons.notifications_none_rounded,
      };

  Color _color(Notificacion n) => switch (n.categoria) {
        'CONSUMO' => AppColors.ambar,
        'MAYORDOMIA' => AppColors.terracota,
        _ => AppColors.primario,
      };

  void _abrir(BuildContext context, Notificacion n) {
    context.read<BandejaCubit>().marcarLeida(n.id);
    final ruta = n.ruta;
    if (ruta == null) return;
    // Las pestañas se abren cambiando de pestaña; el resto se apila
    if (_pestanas.contains(ruta)) {
      context.go(ruta);
    } else {
      context.push(ruta);
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(
        title: const Text('Notificaciones'),
        actions: [
          TextButton(
            onPressed: () => context.read<BandejaCubit>().marcarTodasLeidas(),
            child: const Text('Marcar todas'),
          ),
        ],
      ),
      body: BlocBuilder<BandejaCubit, AsyncState<Bandeja>>(
        builder: (context, state) => AsyncView<Bandeja>(
          state: state,
          onRetry: context.read<BandejaCubit>().cargar,
          onRefresh: context.read<BandejaCubit>().cargar,
          builder: (context, bandeja) {
            if (bandeja.items.isEmpty) {
              return ListView(
                children: const [
                  SizedBox(height: 120),
                  MensajeCentrado(icono: Icons.notifications_off_outlined, texto: 'No tienes avisos por ahora.'),
                ],
              );
            }
            return ListView.separated(
              padding: const EdgeInsets.only(bottom: 24),
              itemCount: bandeja.items.length,
              separatorBuilder: (_, _) => const Divider(height: 1, indent: 72),
              itemBuilder: (context, i) {
                final n = bandeja.items[i];
                return ListTile(
                  onTap: () => _abrir(context, n),
                  contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
                  leading: CircleAvatar(
                    backgroundColor: _color(n).withValues(alpha: 0.14),
                    child: Icon(_icono(n), color: _color(n), size: 22),
                  ),
                  title: Text(n.titulo, style: t.titleSmall?.copyWith(fontWeight: n.leida ? FontWeight.w500 : FontWeight.w800)),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (n.cuerpo != null) Text(n.cuerpo!),
                      const SizedBox(height: 2),
                      Text(Formato.fechaCorta(n.fecha), style: t.labelSmall?.copyWith(color: AppColors.textoTenue)),
                    ],
                  ),
                  trailing: n.leida ? null : const CircleAvatar(radius: 5, backgroundColor: AppColors.terracota),
                );
              },
            );
          },
        ),
      ),
    );
  }
}
