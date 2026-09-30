import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/domain/dimension.dart';
import '../../../../core/domain/rol_usuario.dart';
import '../../../../core/domain/semaforo.dart';
import '../../../../core/router/rutas.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/colores.dart';
import '../../../../core/utils/formato.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../../auth/domain/entities/perfil.dart';
import '../../../auth/presentation/cubit/perfil_cubit.dart';
import '../../domain/entities/reportes_entities.dart';
import '../cubit/reportes_cubits.dart';

/// Reportes: el análisis que responde "¿por qué estoy en amarillo o rojo?" y el reporte integral 4T.
class ReportesPage extends StatefulWidget {
  const ReportesPage({super.key});

  @override
  State<ReportesPage> createState() => _ReportesPageState();
}

class _ReportesPageState extends State<ReportesPage> {
  bool _ver4T = false;

  Future<void> _mover(int delta) async {
    await Future.wait([
      context.read<ReporteFinancieroCubit>().moverMes(delta),
      context.read<ReporteMayordomiaCubit>().moverMes(delta),
    ]);
    if (mounted) setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final cubit = context.read<ReporteFinancieroCubit>();
    return Scaffold(
      appBar: AppBar(title: const Text('Reportes')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 4),
            child: SizedBox(
              width: double.infinity,
              child: SegmentedButton<bool>(
                segments: const [
                  ButtonSegment(value: false, label: Text('Finanzas'), icon: Icon(Icons.pie_chart_outline)),
                  ButtonSegment(value: true, label: Text('Mayordomía 4T'), icon: Icon(Icons.diversity_3_outlined)),
                ],
                selected: {_ver4T},
                onSelectionChanged: (s) => setState(() => _ver4T = s.first),
              ),
            ),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              IconButton(tooltip: 'Mes anterior', icon: const Icon(Icons.chevron_left), onPressed: () => _mover(-1)),
              SizedBox(
                width: 170,
                child: Text(Formato.mesLargo(cubit.periodo),
                    textAlign: TextAlign.center, style: Theme.of(context).textTheme.titleMedium),
              ),
              IconButton(
                tooltip: 'Mes siguiente',
                icon: const Icon(Icons.chevron_right),
                onPressed: cubit.esMesActual ? null : () => _mover(1),
              ),
            ],
          ),
          Expanded(child: _ver4T ? const _Vista4T() : const _VistaFinanzas()),
        ],
      ),
    );
  }
}

// ------------------------------------------------------------------ Finanzas

String _veredicto(Semaforo? s) => switch (s) {
      Semaforo.verde => 'Tu nivel de gasto es adecuado y te permite ahorrar.',
      Semaforo.amarillo => 'Vas ajustado este mes: tus gastos están cerca del límite saludable.',
      Semaforo.rojo => 'Tus gastos superan tu ingreso disponible este mes.',
      null => 'Registra tus ingresos del mes para ver tu semáforo.',
    };

class _VistaFinanzas extends StatelessWidget {
  const _VistaFinanzas();

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<ReporteFinancieroCubit, AsyncState<ReporteFinanciero>>(
      builder: (context, state) => AsyncView<ReporteFinanciero>(
        state: state,
        onRetry: context.read<ReporteFinancieroCubit>().cargar,
        onRefresh: context.read<ReporteFinancieroCubit>().cargar,
        builder: (context, r) {
          final t = Theme.of(context).textTheme;
          final s = r.semaforo;
          return ListView(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
            children: [
              Tarjeta(
                color: s?.tinte,
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    PuntoSemaforo(s, tamano: 22),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(_veredicto(s), style: t.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                          if (s != null) ...[
                            const SizedBox(height: 6),
                            Text(
                              'Gastos y pagos de deuda: ${Formato.porcentaje(r.ratioTotal)} de tu ingreso (verde: menos de 70%, amarillo: 70–90%, rojo: más de 90%).',
                              style: t.bodySmall,
                            ),
                          ],
                          if (r.principalDesvio != null) ...[
                            const SizedBox(height: 6),
                            Text(
                              '${r.principalDesvio!.nombre} superó su presupuesto en ${Formato.porcentaje(r.principalDesvio!.variacionPct ?? 0)}.',
                              style: t.bodySmall?.copyWith(fontWeight: FontWeight.w600),
                            ),
                          ],
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(child: _Ratio(titulo: 'Gasto', valor: r.ratioGasto, color: AppColors.gasto)),
                  const SizedBox(width: 10),
                  Expanded(child: _Ratio(titulo: 'Deuda', valor: r.ratioDeuda, color: AppColors.pagoDeuda)),
                  const SizedBox(width: 10),
                  Expanded(child: _Ratio(titulo: 'Ahorro', valor: r.ratioAhorro, color: AppColors.verde)),
                ],
              ),
              const TituloSeccion('Presupuestado vs. real'),
              if (r.categorias.isEmpty)
                const Tarjeta(child: Text('Aún no tienes categorías. Créalas en Configuración.'))
              else
                Tarjeta(
                  child: Column(children: [for (final l in r.categorias) _FilaPresupuesto(linea: l)]),
                ),
              const SizedBox(height: 12),
              Tarjeta(
                child: Column(
                  children: [
                    FilaDato('Ingresos', Formato.moneda(r.ingresos)),
                    FilaDato('Diezmo y ofrenda apartados', Formato.moneda(r.apartadoTotal), colorValor: AppColors.terracota),
                    FilaDato('Gastos', Formato.moneda(r.gastos)),
                    FilaDato('Pagos de deuda', Formato.moneda(r.pagosDeuda)),
                    FilaDato('Aportes a metas', Formato.moneda(r.aportesMeta)),
                    const Divider(),
                    FilaDato('Saldo disponible', Formato.moneda(r.saldoDisponible), destacado: true),
                  ],
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _Ratio extends StatelessWidget {
  const _Ratio({required this.titulo, required this.valor, required this.color});

  final String titulo;
  final double valor;
  final Color color;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Tarjeta(
      padding: const EdgeInsets.all(14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(titulo, style: t.labelMedium?.copyWith(color: AppColors.textoSecundario)),
          const SizedBox(height: 4),
          Text(Formato.porcentaje(valor), style: t.titleLarge?.copyWith(fontWeight: FontWeight.w800, color: color)),
          Text('del ingreso', style: t.labelSmall?.copyWith(color: AppColors.textoTenue)),
        ],
      ),
    );
  }
}

class _FilaPresupuesto extends StatelessWidget {
  const _FilaPresupuesto({required this.linea});

  final LineaPresupuesto linea;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final color = colorDeHex(linea.color);
    final base = linea.planeado > linea.real ? linea.planeado : linea.real;
    final variacion = linea.variacionPct;
    final colorVar = linea.excedida ? ((variacion ?? 0) > 10 ? AppColors.rojo : AppColors.ambar) : AppColors.verde;
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              CircleAvatar(radius: 6, backgroundColor: color),
              const SizedBox(width: 8),
              Expanded(child: Text(linea.nombre, style: t.bodyMedium?.copyWith(fontWeight: FontWeight.w600))),
              if (variacion != null)
                Text('${variacion > 0 ? '+' : ''}${Formato.porcentaje(variacion)}',
                    style: t.labelMedium?.copyWith(color: colorVar, fontWeight: FontWeight.w700)),
            ],
          ),
          const SizedBox(height: 6),
          BarraProgreso(valor: base <= 0 ? 0 : linea.real / base, color: linea.excedida ? colorVar : color, alto: 6),
          const SizedBox(height: 2),
          Text(
            linea.planeado > 0
                ? '${Formato.moneda(linea.real)} de ${Formato.moneda(linea.planeado)}'
                : '${Formato.moneda(linea.real)} · sin presupuesto',
            style: t.bodySmall?.copyWith(color: AppColors.textoSecundario),
          ),
        ],
      ),
    );
  }
}

// ------------------------------------------------------------------ Mayordomía 4T

class _Vista4T extends StatelessWidget {
  const _Vista4T();

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<ReporteMayordomiaCubit, AsyncState<ReporteMayordomia>>(
      builder: (context, state) => AsyncView<ReporteMayordomia>(
        state: state,
        onRetry: context.read<ReporteMayordomiaCubit>().cargar,
        onRefresh: context.read<ReporteMayordomiaCubit>().cargar,
        builder: (context, r) {
          final t = Theme.of(context).textTheme;
          final s = r.semaforo;
          return ListView(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
            children: [
              Tarjeta(
                color: s?.tinte,
                child: Row(
                  children: [
                    Container(
                      width: 76,
                      height: 76,
                      alignment: Alignment.center,
                      decoration: BoxDecoration(shape: BoxShape.circle, color: Colors.white, border: Border.all(color: s?.color ?? AppColors.borde, width: 5)),
                      child: Text(r.global == null ? '—' : r.global!.round().toString(),
                          style: t.headlineMedium?.copyWith(fontWeight: FontWeight.w800)),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Mayordomía integral', style: t.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                          const SizedBox(height: 4),
                          Text(
                            r.global == null
                                ? 'Registra tus hábitos y tus finanzas para ver tu reporte.'
                                : 'Promedio de las áreas con datos este mes. Es una guía para crecer, no una calificación.',
                            style: t.bodySmall,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 12),
              for (final d in Dimension.values) ...[
                _FilaDimension(dimension: d, puntaje: r.puntajes[d], habitos: r.habitos[d] ?? const []),
                const SizedBox(height: 10),
              ],
              if (r.diezmoAlDia != null)
                Tarjeta(
                  child: Row(
                    children: [
                      Icon(r.diezmoAlDia! ? Icons.check_circle : Icons.schedule, color: r.diezmoAlDia! ? AppColors.verde : AppColors.ambar),
                      const SizedBox(width: 12),
                      Expanded(child: Text(r.diezmoAlDia! ? 'Tu diezmo de este mes está entregado.' : 'Tu diezmo de este mes aún está pendiente de entregar.')),
                    ],
                  ),
                ),
              const SizedBox(height: 12),
              OutlinedButton.icon(
                onPressed: () => context.push(Rutas.habitos),
                icon: const Icon(Icons.edit_calendar_outlined),
                label: const Text('Registrar mis hábitos de hoy'),
              ),
              const SizedBox(height: 12),
              const _CompartirConPastor(),
            ],
          );
        },
      ),
    );
  }
}

const _iconosDimension = {
  Dimension.tiempo: Icons.schedule_outlined,
  Dimension.talento: Icons.volunteer_activism_outlined,
  Dimension.tesoro: Icons.savings_outlined,
  Dimension.templo: Icons.self_improvement_outlined,
};

class _FilaDimension extends StatelessWidget {
  const _FilaDimension({required this.dimension, required this.puntaje, required this.habitos});

  final Dimension dimension;
  final double? puntaje;
  final List<HabitoCumplimiento> habitos;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final color = puntaje == null ? AppColors.textoTenue : (puntaje! >= 75 ? AppColors.verde : (puntaje! >= 50 ? AppColors.ambar : AppColors.rojo));
    return Tarjeta(
      padding: EdgeInsets.zero,
      child: Theme(
        data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
        child: ExpansionTile(
          tilePadding: const EdgeInsets.symmetric(horizontal: 18, vertical: 4),
          leading: Icon(_iconosDimension[dimension], color: AppColors.primario),
          title: Row(
            children: [
              Expanded(child: Text(dimension.etiqueta, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700))),
              Text(puntaje == null ? 'Sin datos' : puntaje!.round().toString(),
                  style: t.titleMedium?.copyWith(fontWeight: FontWeight.w800, color: color)),
            ],
          ),
          subtitle: Padding(
            padding: const EdgeInsets.only(top: 6),
            child: BarraProgreso(valor: (puntaje ?? 0) / 100, color: color, alto: 6),
          ),
          childrenPadding: const EdgeInsets.fromLTRB(18, 0, 18, 14),
          children: [
            Align(alignment: Alignment.centerLeft, child: Text(dimension.descripcion, style: t.bodySmall?.copyWith(color: AppColors.textoSecundario))),
            const SizedBox(height: 8),
            if (dimension == Dimension.tesoro)
              Text('Fidelidad en el diezmo, semáforo de gasto, ahorro y salud de tus deudas.', style: t.bodySmall)
            else if (habitos.isEmpty)
              Text('Aún no registraste hábitos de esta área este mes.', style: t.bodySmall)
            else
              for (final h in habitos)
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 4),
                  child: Row(
                    children: [
                      Expanded(child: Text(h.nombre, style: t.bodySmall)),
                      SizedBox(width: 90, child: BarraProgreso(valor: h.cumplimientoPct / 100, alto: 5)),
                      SizedBox(width: 44, child: Text(Formato.porcentaje(h.cumplimientoPct), textAlign: TextAlign.right, style: t.labelSmall)),
                    ],
                  ),
                ),
          ],
        ),
      ),
    );
  }
}

/// El hermano decide si su pastor ve su reporte (nombre, puntajes, hábitos y diezmo sí/no; nunca montos).
class _CompartirConPastor extends StatelessWidget {
  const _CompartirConPastor();

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<PerfilCubit, AsyncState<Perfil>>(
      builder: (context, state) {
        final perfil = state.datos;
        // Solo un hermano puede compartir; el pastor no
        if (perfil == null || perfil.rol != RolUsuario.hermano) return const SizedBox.shrink();
        return Tarjeta(
          color: AppColors.terracotaTinte,
          child: SwitchListTile(
            contentPadding: EdgeInsets.zero,
            title: const Text('Compartir mi reporte con mi pastor'),
            subtitle: const Text('Verá tu nombre, tus puntajes, tus hábitos y si entregaste tu diezmo. Nunca verá tus montos ni tus movimientos. Puedes dejar de compartir cuando quieras.'),
            value: perfil.comparteReporte,
            onChanged: (v) async {
              final error = await context.read<PerfilCubit>().compartirReporte(v);
              if (context.mounted) {
                avisar(context, error ?? (v ? 'Tu pastor ya puede ver tu reporte' : 'Dejaste de compartir tu reporte'), error: error != null);
              }
            },
          ),
        );
      },
    );
  }
}
