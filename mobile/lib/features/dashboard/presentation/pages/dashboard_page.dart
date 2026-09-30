import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/domain/semaforo.dart';
import '../../../../core/router/rutas.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/formato.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../../auth/presentation/bloc/auth_bloc.dart';
import '../../domain/entities/inicio.dart';
import '../cubit/inicio_cubit.dart';

/// Inicio: "¿cómo estoy este mes?" en 10 segundos (spec 4.3).
class DashboardPage extends StatelessWidget {
  const DashboardPage({super.key});

  static const _pestanas = {Rutas.inicio, Rutas.movimientos, Rutas.diezmos, Rutas.metas, Rutas.reportes};

  /// Las pestañas se abren cambiando de pestaña; el resto se apila encima y, al volver, se refresca Inicio.
  Future<void> _abrir(BuildContext context, String ruta) async {
    if (_pestanas.contains(ruta)) {
      context.go(ruta);
      return;
    }
    final cubit = context.read<InicioCubit>();
    await context.push(ruta);
    cubit.cargar();
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthBloc>().state;
    final sesion = auth is AuthAutenticado ? auth.sesion : null;
    return Scaffold(
      appBar: AppBar(
        title: Text('SelahFinance',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800, color: AppColors.primario)),
        actions: [
          if (sesion?.esPastor ?? false)
            IconButton(
              tooltip: 'Mi congregación',
              icon: const Icon(Icons.groups_outlined),
              onPressed: () => _abrir(context, Rutas.congregacion),
            ),
          BlocBuilder<InicioCubit, AsyncState<Inicio>>(
            builder: (context, state) {
              final n = state.datos?.notificacionesNoLeidas ?? 0;
              return IconButton(
                tooltip: 'Notificaciones',
                icon: Badge(
                  isLabelVisible: n > 0,
                  label: Text('$n'),
                  child: const Icon(Icons.notifications_none_rounded),
                ),
                onPressed: () => _abrir(context, Rutas.notificaciones),
              );
            },
          ),
          IconButton(
            tooltip: 'Perfil y configuración',
            icon: CircleAvatar(
              radius: 15,
              backgroundColor: AppColors.primario,
              child: Text((sesion?.nombres ?? '?').characters.first.toUpperCase(),
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700, fontSize: 14)),
            ),
            onPressed: () => _abrir(context, Rutas.configuracion),
          ),
        ],
      ),
      body: BlocBuilder<InicioCubit, AsyncState<Inicio>>(
        builder: (context, state) => AsyncView<Inicio>(
          state: state,
          onRetry: context.read<InicioCubit>().cargar,
          onRefresh: context.read<InicioCubit>().cargar,
          builder: (context, inicio) => _Contenido(inicio: inicio, sesionNombre: sesion?.nombres, abrir: (r) => _abrir(context, r)),
        ),
      ),
    );
  }
}

String _veredicto(Semaforo? s) => switch (s) {
      Semaforo.verde => 'Tu nivel de gasto es adecuado y te permite ahorrar.',
      Semaforo.amarillo => 'Vas ajustado este mes: tus gastos están cerca del límite saludable.',
      Semaforo.rojo => 'Tus gastos superan tu ingreso disponible este mes.',
      null => 'Registra tu primer ingreso del mes para ver tu semáforo.',
    };

class _Contenido extends StatelessWidget {
  const _Contenido({required this.inicio, required this.sesionNombre, required this.abrir});

  final Inicio inicio;
  final String? sesionNombre;
  final void Function(String ruta) abrir;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final s = inicio.semaforo;
    final base = inicio.ingresos <= 0 ? 1.0 : inicio.ingresos;
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 4, 20, 32),
      children: [
        Text(sesionNombre == null ? Formato.mesLargo(inicio.periodo) : 'Hola, $sesionNombre',
            style: t.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
        Text(Formato.mesLargo(inicio.periodo), style: t.bodyMedium?.copyWith(color: AppColors.textoSecundario)),
        const SizedBox(height: 16),

        // 1. Semáforo
        Tarjeta(
          color: s?.tinte,
          onTap: () => abrir(Rutas.reportes),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              PuntoSemaforo(s, tamano: 20),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(_veredicto(s), style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
                    if (inicio.categoriaDesvio != null)
                      Padding(
                        padding: const EdgeInsets.only(top: 4),
                        child: Text(
                          '${inicio.categoriaDesvio} superó su presupuesto en ${Formato.porcentaje(inicio.variacionDesvio ?? 0)}.',
                          style: t.bodySmall,
                        ),
                      ),
                  ],
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),

        // 2. Saldo disponible real (ya sin diezmo ni ofrenda)
        Tarjeta(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Saldo disponible', style: t.labelLarge?.copyWith(color: AppColors.textoSecundario)),
              const SizedBox(height: 4),
              Text(Formato.monedaEntera(inicio.saldoDisponible),
                  style: t.displaySmall?.copyWith(fontWeight: FontWeight.w800, color: inicio.saldoDisponible < 0 ? AppColors.rojo : AppColors.texto)),
              const SizedBox(height: 4),
              Row(
                children: [
                  const Icon(Icons.volunteer_activism_outlined, size: 16, color: AppColors.terracota),
                  const SizedBox(width: 6),
                  Expanded(
                    child: Text('Diezmo y ofrenda ya apartados: ${Formato.moneda(inicio.apartadoTotal)}',
                        style: t.bodySmall?.copyWith(color: AppColors.terracota)),
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),

        // 3. Ingresos vs gastos
        Tarjeta(
          onTap: () => abrir(Rutas.reportes),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Ingresos vs. gastos', style: t.labelLarge?.copyWith(color: AppColors.textoSecundario)),
              const SizedBox(height: 12),
              FilaDato('Ingresos', Formato.moneda(inicio.ingresos), colorValor: AppColors.ingreso),
              BarraProgreso(valor: 1, color: AppColors.ingreso, alto: 6),
              const SizedBox(height: 10),
              FilaDato('Gastos', Formato.moneda(inicio.gastos)),
              BarraProgreso(valor: inicio.gastos / base, color: AppColors.gasto, alto: 6),
            ],
          ),
        ),

        // 4. Diezmo pendiente
        if (inicio.diezmoPendiente > 0) ...[
          const SizedBox(height: 12),
          Tarjeta(
            color: AppColors.terracotaTinte,
            onTap: () => abrir(Rutas.diezmos),
            child: Row(
              children: [
                const Icon(Icons.volunteer_activism, color: AppColors.terracota),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Diezmo pendiente de entregar', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700, color: AppColors.terracotaOscuro)),
                      Text(Formato.moneda(inicio.diezmoPendiente), style: t.bodyMedium?.copyWith(color: AppColors.terracotaOscuro)),
                    ],
                  ),
                ),
                const Icon(Icons.chevron_right),
              ],
            ),
          ),
        ],

        // 5. Meta principal
        if (inicio.metaPrincipal != null) ...[
          const SizedBox(height: 12),
          Tarjeta(
            onTap: () => abrir(Rutas.metas),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    const Icon(Icons.flag_outlined, color: AppColors.primario),
                    const SizedBox(width: 8),
                    Expanded(child: Text(inicio.metaPrincipal!.nombre, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700))),
                    Text(Formato.porcentaje(inicio.metaPrincipal!.porcentaje), style: t.titleSmall?.copyWith(fontWeight: FontWeight.w800)),
                  ],
                ),
                const SizedBox(height: 10),
                BarraProgreso(valor: inicio.metaPrincipal!.porcentaje / 100),
                const SizedBox(height: 6),
                Text('${Formato.moneda(inicio.metaPrincipal!.montoActual)} de ${Formato.moneda(inicio.metaPrincipal!.montoObjetivo)}',
                    style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
              ],
            ),
          ),
        ],

        // 6. Reflexión semanal (viernes por la tarde y sábado)
        if (inicio.reflexionVisible) ...[
          const SizedBox(height: 12),
          Tarjeta(
            color: AppColors.primario,
            onTap: () => abrir(Rutas.reflexion),
            child: Row(
              children: [
                const Icon(Icons.wb_twilight_outlined, color: Colors.white),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Reflexión de la semana', style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700, color: Colors.white)),
                      Text(inicio.reflexionRespondida ? 'Ya la respondiste. Puedes releerla.' : 'Un momento para revisar y descansar.',
                          style: t.bodySmall?.copyWith(color: Colors.white70)),
                    ],
                  ),
                ),
                const Icon(Icons.chevron_right, color: Colors.white),
              ],
            ),
          ),
        ],
        const SizedBox(height: 12),
        Tarjeta(
          onTap: () => abrir(Rutas.habitos),
          child: Row(
            children: [
              const Icon(Icons.edit_calendar_outlined, color: AppColors.primario),
              const SizedBox(width: 12),
              Expanded(child: Text('Mis hábitos de hoy: tiempo, talento y templo', style: t.bodyMedium)),
              const Icon(Icons.chevron_right),
            ],
          ),
        ),
      ],
    );
  }
}
