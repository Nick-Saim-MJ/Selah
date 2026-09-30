import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/bloc/datos_cambiados.dart';
import '../../../../core/router/rutas.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/formato.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../domain/entities/movimiento.dart';
import '../../domain/entities/tipo_movimiento.dart';
import '../bloc/movimientos_bloc.dart';
import '../widgets/movimiento_tile.dart';
import '../widgets/selector_tipo_sheet.dart';
import '../widgets/tipo_movimiento_ui.dart';

/// Movimientos: la única pantalla que captura dinero (Lucas 16:10).
class MovimientosPage extends StatelessWidget {
  const MovimientosPage({super.key});

  static const _filtros = <(String, TipoMovimiento?)>[
    ('Todos', null),
    ('Ingresos', TipoMovimiento.ingreso),
    ('Gastos', TipoMovimiento.gasto),
    ('Diezmos', TipoMovimiento.diezmo),
    ('Ofrendas', TipoMovimiento.ofrenda),
    ('Deudas', TipoMovimiento.pagoDeuda),
    ('Metas', TipoMovimiento.aporteMeta),
  ];

  Future<void> _nuevo(BuildContext context) async {
    final tipo = await mostrarSelectorTipo(context);
    if (tipo == null || !context.mounted) return;
    final guardado = await context.push<bool>(Rutas.nuevoMovimiento(tipo));
    if (guardado == true && context.mounted) {
      context.read<MovimientosBloc>().add(const MovimientosSolicitados());
    }
  }

  @override
  Widget build(BuildContext context) {
    final texto = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Movimientos')),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _nuevo(context),
        tooltip: 'Nuevo movimiento',
        child: const Icon(Icons.add),
      ),
      body: BlocListener<MovimientosBloc, MovimientosState>(
        listenWhen: (a, b) => (b.error != null && a.error != b.error && b.items.isNotEmpty) || (!a.eliminado && b.eliminado),
        listener: (context, state) {
          avisar(context, state.eliminado ? 'Movimiento eliminado' : state.error!, error: !state.eliminado);
          if (state.eliminado) datosCambiados.avisar(context.read<MovimientosBloc>());
        },
        child: BlocBuilder<MovimientosBloc, MovimientosState>(
          builder: (context, state) {
            return Column(
              children: [
                SizedBox(
                  height: 48,
                  child: ListView.separated(
                    scrollDirection: Axis.horizontal,
                    padding: const EdgeInsets.symmetric(horizontal: 20),
                    itemCount: _filtros.length,
                    separatorBuilder: (_, _) => const SizedBox(width: 8),
                    itemBuilder: (context, i) {
                      final (etiqueta, tipo) = _filtros[i];
                      return ChoiceChip(
                        label: Text(etiqueta),
                        selected: state.filtro == tipo,
                        onSelected: (_) => context.read<MovimientosBloc>().add(MovimientosFiltroCambiado(tipo)),
                      );
                    },
                  ),
                ),
                Expanded(child: _Contenido(state: state, texto: texto)),
              ],
            );
          },
        ),
      ),
    );
  }
}

class _Contenido extends StatelessWidget {
  const _Contenido({required this.state, required this.texto});

  final MovimientosState state;
  final TextTheme texto;

  @override
  Widget build(BuildContext context) {
    if (state.items.isEmpty) {
      return switch (state.estado) {
        EstadoCarga.inicial || EstadoCarga.cargando => const Center(child: CircularProgressIndicator()),
        EstadoCarga.fallo => MensajeCentrado(
            icono: Icons.cloud_off_outlined,
            texto: state.error ?? 'No se pudo cargar',
            accion: 'Reintentar',
            onAccion: () => context.read<MovimientosBloc>().add(const MovimientosSolicitados()),
          ),
        EstadoCarga.exito => const MensajeCentrado(
            icono: Icons.receipt_long_outlined,
            texto: 'Aún no hay movimientos. Registra tu primer ingreso con el botón +',
          ),
      };
    }

    final grupos = state.porFecha.entries.toList();
    return RefreshIndicator(
      onRefresh: () async => context.read<MovimientosBloc>().add(const MovimientosSolicitados()),
      child: NotificationListener<ScrollNotification>(
        onNotification: (n) {
          if (n.metrics.extentAfter < 300) {
            context.read<MovimientosBloc>().add(const MovimientosMasSolicitados());
          }
          return false;
        },
        child: ListView.builder(
          padding: const EdgeInsets.only(bottom: 96),
          itemCount: grupos.length + (state.hayMas ? 1 : 0),
          itemBuilder: (context, i) {
            if (i == grupos.length) {
              return const Padding(padding: EdgeInsets.all(16), child: Center(child: CircularProgressIndicator()));
            }
            final grupo = grupos[i];
            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 16, 20, 4),
                  child: Text(Formato.fechaCorta(grupo.key).toUpperCase(),
                      style: texto.labelSmall?.copyWith(color: AppColors.textoTenue, letterSpacing: 0.8)),
                ),
                for (final m in grupo.value) MovimientoTile(movimiento: m, onTap: () => _detalle(context, m)),
              ],
            );
          },
        ),
      ),
    );
  }

  /// Detalle del movimiento con la opción de eliminarlo (un registro financiero se borra de forma lógica).
  Future<void> _detalle(BuildContext context, Movimiento m) async {
    final bloc = context.read<MovimientosBloc>();
    final eliminar = await showModalBottomSheet<bool>(
      context: context,
      useRootNavigator: true,
      showDragHandle: true,
      backgroundColor: AppColors.superficie,
      builder: (context) => _DetalleMovimiento(movimiento: m),
    );
    if (eliminar != true || !context.mounted) return;
    if (await confirmar(context,
        titulo: 'Eliminar movimiento',
        mensaje: m.tipo == TipoMovimiento.ingreso
            ? 'También se anulará el diezmo y la ofrenda que quedaron apartados por este ingreso, si aún no los entregaste.'
            : 'Se quitará de tus listas y tus saldos se recalcularán.',
        aceptar: 'Eliminar')) {
      bloc.add(MovimientoEliminacionSolicitada(m.id));
    }
  }
}

class _DetalleMovimiento extends StatelessWidget {
  const _DetalleMovimiento({required this.movimiento});

  final Movimiento movimiento;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final m = movimiento;
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 0, 20, 16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                CircleAvatar(backgroundColor: m.tipo.color.withValues(alpha: 0.14), child: Icon(m.tipo.icono, color: m.tipo.color)),
                const SizedBox(width: 12),
                Expanded(child: Text(m.descripcion ?? m.tipo.etiqueta, style: t.titleMedium?.copyWith(fontWeight: FontWeight.w700))),
              ],
            ),
            const SizedBox(height: 16),
            FilaDato('Tipo', m.tipo.etiqueta),
            FilaDato('Monto', Formato.moneda(m.monto), destacado: true),
            FilaDato('Fecha', Formato.fechaLarga(m.fecha)),
            if (m.tipo == TipoMovimiento.gasto) FilaDato('Presupuestado', m.esPresupuestado ? 'Sí' : 'No, fue un imprevisto'),
            const SizedBox(height: 12),
            OutlinedButton.icon(
              onPressed: () => Navigator.pop(context, true),
              style: OutlinedButton.styleFrom(foregroundColor: AppColors.rojo),
              icon: const Icon(Icons.delete_outline),
              label: const Text('Eliminar'),
            ),
          ],
        ),
      ),
    );
  }
}
