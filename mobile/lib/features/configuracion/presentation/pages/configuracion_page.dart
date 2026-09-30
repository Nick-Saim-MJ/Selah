import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/domain/rol_usuario.dart';
import '../../../../core/router/rutas.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../../auth/domain/entities/perfil.dart';
import '../../../auth/presentation/bloc/auth_bloc.dart';
import '../../../auth/presentation/cubit/perfil_cubit.dart';
import '../../../mayordomia/domain/entities/mayordomia_entities.dart';
import '../../../mayordomia/presentation/cubit/mayordomia_cubits.dart';
import '../../../mayordomia/presentation/widgets/config_mayordomia_form.dart';
import '../../../notificaciones/domain/notificaciones_domain.dart';
import '../../../notificaciones/presentation/cubit/notificaciones_cubits.dart';

/// Perfil y configuración (spec 4.8). El administrador ve solo su cuenta y la seguridad.
class ConfiguracionPage extends StatelessWidget {
  const ConfiguracionPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Perfil y configuración')),
      body: BlocBuilder<PerfilCubit, AsyncState<Perfil>>(
        builder: (context, state) => AsyncView<Perfil>(
          state: state,
          onRetry: context.read<PerfilCubit>().cargar,
          builder: (context, perfil) => ListView(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 32),
            children: [
              _TarjetaPerfil(perfil: perfil),
              if (perfil.rol == RolUsuario.hermano) ...[
                const TituloSeccion('Privacidad'),
                Tarjeta(
                  child: SwitchListTile(
                    contentPadding: EdgeInsets.zero,
                    title: const Text('Compartir mi reporte con mi pastor'),
                    subtitle: const Text('Verá tu nombre, puntajes, hábitos y si entregaste tu diezmo. Nunca tus montos ni tus movimientos.'),
                    value: perfil.comparteReporte,
                    onChanged: (v) async {
                      final error = await context.read<PerfilCubit>().compartirReporte(v);
                      if (context.mounted && error != null) avisar(context, error, error: true);
                    },
                  ),
                ),
              ],
              if (perfil.rol.tieneFinanzas) ...[
                const TituloSeccion('Mayordomía'),
                const _SeccionMayordomia(),
                const TituloSeccion('Mi presupuesto'),
                Tarjeta(
                  padding: EdgeInsets.zero,
                  child: Column(
                    children: [
                      ListTile(
                        leading: const Icon(Icons.payments_outlined),
                        title: const Text('Fuentes de ingreso'),
                        trailing: const Icon(Icons.chevron_right),
                        onTap: () => context.push(Rutas.fuentesIngreso),
                      ),
                      const Divider(height: 1),
                      ListTile(
                        leading: const Icon(Icons.category_outlined),
                        title: const Text('Categorías y presupuesto'),
                        trailing: const Icon(Icons.chevron_right),
                        onTap: () => context.push(Rutas.categorias),
                      ),
                    ],
                  ),
                ),
                const TituloSeccion('Notificaciones'),
                const _SeccionNotificaciones(),
                const TituloSeccion('Mi vida espiritual'),
                Tarjeta(
                  padding: EdgeInsets.zero,
                  child: Column(
                    children: [
                      ListTile(
                        leading: const Icon(Icons.edit_calendar_outlined),
                        title: const Text('Mis hábitos de hoy'),
                        trailing: const Icon(Icons.chevron_right),
                        onTap: () => context.push(Rutas.habitos),
                      ),
                      const Divider(height: 1),
                      ListTile(
                        leading: const Icon(Icons.wb_twilight_outlined),
                        title: const Text('Reflexión de la semana'),
                        trailing: const Icon(Icons.chevron_right),
                        onTap: () => context.push(Rutas.reflexion),
                      ),
                    ],
                  ),
                ),
              ],
              const TituloSeccion('Seguridad'),
              Tarjeta(
                padding: EdgeInsets.zero,
                child: Column(
                  children: [
                    ListTile(
                      leading: const Icon(Icons.lock_outline),
                      title: const Text('Cambiar contraseña'),
                      trailing: const Icon(Icons.chevron_right),
                      onTap: () => context.push(Rutas.cambiarPassword),
                    ),
                    const Divider(height: 1),
                    ListTile(
                      leading: const Icon(Icons.logout, color: AppColors.rojo),
                      title: const Text('Cerrar sesión', style: TextStyle(color: AppColors.rojo)),
                      onTap: () async {
                        final auth = context.read<AuthBloc>();
                        if (await confirmar(context, titulo: 'Cerrar sesión', mensaje: '¿Quieres salir de tu cuenta?', aceptar: 'Salir')) {
                          auth.add(const AuthCierreSolicitado());
                        }
                      },
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _TarjetaPerfil extends StatelessWidget {
  const _TarjetaPerfil({required this.perfil});

  final Perfil perfil;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Tarjeta(
      child: Row(
        children: [
          CircleAvatar(
            radius: 28,
            backgroundColor: AppColors.primario,
            child: Text(perfil.nombres.characters.first.toUpperCase(),
                style: t.titleLarge?.copyWith(color: Colors.white, fontWeight: FontWeight.w800)),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(perfil.nombreCompleto, style: t.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                Text(perfil.email, style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
                const SizedBox(height: 6),
                Wrap(
                  spacing: 6,
                  children: [
                    Etiqueta(perfil.rol.etiqueta),
                    if (perfil.iglesiaNombre != null) Etiqueta(perfil.iglesiaNombre!, color: AppColors.terracota),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// Porcentajes, día de entrega y Modo Sábado. Se guardan con un botón.
class _SeccionMayordomia extends StatefulWidget {
  const _SeccionMayordomia();

  @override
  State<_SeccionMayordomia> createState() => _SeccionMayordomiaState();
}

class _SeccionMayordomiaState extends State<_SeccionMayordomia> {
  ConfigMayordomia? _cambios;
  bool _guardando = false;

  Future<void> _guardar() async {
    final cambios = _cambios;
    if (cambios == null) return;
    setState(() => _guardando = true);
    final error = await context.read<ConfigMayordomiaCubit>().guardar(cambios);
    if (!mounted) return;
    setState(() {
      _guardando = false;
      if (error == null) _cambios = null;
    });
    avisar(context, error ?? 'Configuración guardada', error: error != null);
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<ConfigMayordomiaCubit, AsyncState<ConfigMayordomia>>(
      builder: (context, state) => AsyncView<ConfigMayordomia>(
        state: state,
        onRetry: context.read<ConfigMayordomiaCubit>().cargar,
        builder: (context, config) => Tarjeta(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              ConfigMayordomiaForm(inicial: config, onCambio: (c) => setState(() => _cambios = c)),
              const SizedBox(height: 12),
              FilledButton(
                onPressed: _cambios == null || _guardando ? null : _guardar,
                child: _guardando
                    ? const SizedBox.square(dimension: 22, child: CircularProgressIndicator(strokeWidth: 2.5))
                    : const Text('Guardar cambios'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SeccionNotificaciones extends StatelessWidget {
  const _SeccionNotificaciones();

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<PreferenciasNotificacionCubit, AsyncState<PreferenciasNotificacion>>(
      builder: (context, state) => AsyncView<PreferenciasNotificacion>(
        state: state,
        onRetry: context.read<PreferenciasNotificacionCubit>().cargar,
        builder: (context, p) {
          Future<void> cambiar(PreferenciasNotificacion nueva) async {
            final error = await context.read<PreferenciasNotificacionCubit>().guardar(nueva);
            if (context.mounted && error != null) avisar(context, error, error: true);
          }

          return Tarjeta(
            padding: const EdgeInsets.symmetric(vertical: 4),
            child: Column(
              children: [
                SwitchListTile(
                  title: const Text('Recordatorio de diezmo'),
                  value: p.recordatorioDiezmo,
                  onChanged: (v) => cambiar(p.copyWith(recordatorioDiezmo: v)),
                ),
                SwitchListTile(
                  title: const Text('Recordatorio de cuotas de deuda'),
                  subtitle: const Text('Se posponen durante el Modo Sábado'),
                  value: p.recordatorioDeudas,
                  onChanged: (v) => cambiar(p.copyWith(recordatorioDeudas: v)),
                ),
                SwitchListTile(
                  title: const Text('Resumen semanal'),
                  value: p.resumenSemanal,
                  onChanged: (v) => cambiar(p.copyWith(resumenSemanal: v)),
                ),
                SwitchListTile(
                  title: const Text('Invitación a la reflexión del viernes'),
                  value: p.reflexionSabado,
                  onChanged: (v) => cambiar(p.copyWith(reflexionSabado: v)),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
