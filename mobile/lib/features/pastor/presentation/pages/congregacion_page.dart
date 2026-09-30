import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/domain/dimension.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/formato.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../domain/pastor_domain.dart';
import '../cubit/congregacion_cubit.dart';

/// Mi congregación: totales anónimos + reportes de quienes decidieron compartir el suyo.
class CongregacionPage extends StatelessWidget {
  const CongregacionPage({super.key});

  @override
  Widget build(BuildContext context) {
    final cubit = context.watch<CongregacionCubit>();
    return Scaffold(
      appBar: AppBar(title: const Text('Mi congregación')),
      body: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              IconButton(tooltip: 'Mes anterior', icon: const Icon(Icons.chevron_left), onPressed: () => cubit.moverMes(-1)),
              SizedBox(
                width: 170,
                child: Text(Formato.mesLargo(cubit.periodo), textAlign: TextAlign.center, style: Theme.of(context).textTheme.titleMedium),
              ),
              IconButton(
                tooltip: 'Mes siguiente',
                icon: const Icon(Icons.chevron_right),
                onPressed: cubit.esMesActual ? null : () => cubit.moverMes(1),
              ),
            ],
          ),
          Expanded(
            child: BlocBuilder<CongregacionCubit, AsyncState<VistaCongregacion>>(
              builder: (context, state) => AsyncView<VistaCongregacion>(
                state: state,
                onRetry: cubit.cargar,
                onRefresh: cubit.cargar,
                builder: (context, vista) => _Contenido(vista: vista),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _Contenido extends StatelessWidget {
  const _Contenido({required this.vista});

  final VistaCongregacion vista;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final r = vista.resumen;
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 4, 20, 32),
      children: [
        Tarjeta(
          child: Row(
            children: [
              Expanded(child: _Cifra(valor: '${r.miembrosActivos}', titulo: 'hermanos activos')),
              Expanded(child: _Cifra(valor: '${r.miembrosConReporte}', titulo: 'con reporte del mes')),
              Expanded(
                child: _Cifra(
                  valor: r.datosSuficientes && r.fidelidadDiezmoPct != null ? Formato.porcentaje(r.fidelidadDiezmoPct!) : '—',
                  titulo: 'diezmo al día',
                ),
              ),
            ],
          ),
        ),
        const TituloSeccion('Promedio de la congregación'),
        if (!r.datosSuficientes)
          Tarjeta(
            color: AppColors.ambarTinte,
            child: Row(
              children: [
                const Icon(Icons.shield_outlined, color: AppColors.ambar),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Para cuidar la privacidad, los promedios se muestran cuando al menos ${r.minimoAnonimato} hermanos tienen datos este mes. Ahora hay ${r.miembrosConReporte}.',
                    style: t.bodyMedium,
                  ),
                ),
              ],
            ),
          )
        else
          Tarjeta(
            child: Column(
              children: [
                for (final d in Dimension.values) _FilaPromedio(dimension: d, valor: r.puntaje(d)),
                const Divider(),
                FilaDato('Global', r.global == null ? 'Sin datos' : r.global!.round().toString(), destacado: true),
              ],
            ),
          ),
        const SizedBox(height: 8),
        Text('Son totales anónimos: nadie puede ser identificado y no incluyen dinero.',
            style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
        const TituloSeccion('Hermanos que comparten su reporte contigo'),
        if (vista.compartidos.isEmpty)
          const Tarjeta(child: Text('Todavía nadie ha decidido compartir su reporte contigo. Ellos eligen si lo hacen.'))
        else
          for (final c in vista.compartidos) ...[_TarjetaCompartida(reporte: c), const SizedBox(height: 10)],
      ],
    );
  }
}

class _Cifra extends StatelessWidget {
  const _Cifra({required this.valor, required this.titulo});

  final String valor;
  final String titulo;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Column(
      children: [
        Text(valor, style: t.headlineMedium?.copyWith(fontWeight: FontWeight.w800, color: AppColors.primario)),
        Text(titulo, textAlign: TextAlign.center, style: t.labelSmall?.copyWith(color: AppColors.textoSecundario)),
      ],
    );
  }
}

class _FilaPromedio extends StatelessWidget {
  const _FilaPromedio({required this.dimension, required this.valor});

  final Dimension dimension;
  final double? valor;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        children: [
          SizedBox(width: 72, child: Text(dimension.etiqueta, style: t.bodyMedium)),
          Expanded(child: BarraProgreso(valor: (valor ?? 0) / 100, alto: 8)),
          SizedBox(
            width: 64,
            child: Text(valor == null ? 'Pocos datos' : valor!.round().toString(),
                textAlign: TextAlign.right, style: t.labelMedium?.copyWith(fontWeight: FontWeight.w700)),
          ),
        ],
      ),
    );
  }
}

class _TarjetaCompartida extends StatelessWidget {
  const _TarjetaCompartida({required this.reporte});

  final ReporteCompartido reporte;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final r = reporte;
    return Tarjeta(
      padding: EdgeInsets.zero,
      child: Theme(
        data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
        child: ExpansionTile(
          tilePadding: const EdgeInsets.symmetric(horizontal: 18, vertical: 4),
          leading: PuntoSemaforo(r.semaforo, tamano: 18),
          title: Text(r.nombre, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
          subtitle: Text(
            r.diezmoAlDia == null ? 'Sin diezmo este mes' : (r.diezmoAlDia! ? 'Diezmo al día' : 'Diezmo pendiente'),
            style: t.bodySmall?.copyWith(color: r.diezmoAlDia == false ? AppColors.ambar : AppColors.textoSecundario),
          ),
          trailing: Text(r.global == null ? '—' : r.global!.round().toString(),
              style: t.titleLarge?.copyWith(fontWeight: FontWeight.w800)),
          childrenPadding: const EdgeInsets.fromLTRB(18, 0, 18, 14),
          children: [
            for (final d in Dimension.values) ...[
              Row(
                children: [
                  SizedBox(width: 72, child: Text(d.etiqueta, style: t.bodySmall)),
                  Expanded(child: BarraProgreso(valor: (r.puntajes[d] ?? 0) / 100, alto: 6)),
                  SizedBox(
                    width: 56,
                    child: Text(r.puntajes[d] == null ? 'Sin datos' : r.puntajes[d]!.round().toString(),
                        textAlign: TextAlign.right, style: t.labelSmall),
                  ),
                ],
              ),
              for (final h in r.habitos[d] ?? const <HabitoCompartido>[])
                Padding(
                  padding: const EdgeInsets.only(left: 12, top: 2),
                  child: Row(
                    children: [
                      Expanded(child: Text(h.nombre, style: t.labelSmall?.copyWith(color: AppColors.textoSecundario))),
                      Text(Formato.porcentaje(h.cumplimientoPct), style: t.labelSmall),
                    ],
                  ),
                ),
              const SizedBox(height: 8),
            ],
          ],
        ),
      ),
    );
  }
}
