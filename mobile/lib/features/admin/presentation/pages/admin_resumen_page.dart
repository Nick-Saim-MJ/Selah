import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/domain/rol_usuario.dart';
import '../../../../core/router/rutas.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../../auth/presentation/bloc/auth_bloc.dart';
import '../../domain/entities/admin_entities.dart';
import '../cubit/admin_cubits.dart';

/// Panel del administrador: cuántas cuentas hay. El admin gestiona cuentas e iglesias; no ve finanzas de nadie.
class AdminResumenPage extends StatelessWidget {
  const AdminResumenPage({super.key});

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthBloc>().state;
    final nombre = auth is AuthAutenticado ? auth.sesion.nombres : null;
    return Scaffold(
      appBar: AppBar(
        title: Text('SelahFinance · Admin',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800, color: AppColors.primario)),
        actions: [
          PopupMenuButton<String>(
            icon: const Icon(Icons.account_circle_outlined),
            onSelected: (v) async {
              if (v == 'password') {
                context.push(Rutas.cambiarPassword);
              } else {
                final bloc = context.read<AuthBloc>();
                if (await confirmar(context, titulo: 'Cerrar sesión', mensaje: '¿Quieres salir de tu cuenta?', aceptar: 'Salir')) {
                  bloc.add(const AuthCierreSolicitado());
                }
              }
            },
            itemBuilder: (_) => const [
              PopupMenuItem(value: 'password', child: Text('Cambiar contraseña')),
              PopupMenuItem(value: 'salir', child: Text('Cerrar sesión')),
            ],
          ),
        ],
      ),
      body: BlocBuilder<AdminResumenCubit, AsyncState<ResumenUsuarios>>(
        builder: (context, state) => AsyncView<ResumenUsuarios>(
          state: state,
          onRetry: context.read<AdminResumenCubit>().cargar,
          onRefresh: context.read<AdminResumenCubit>().cargar,
          builder: (context, r) {
            final t = Theme.of(context).textTheme;
            return ListView(
              padding: const EdgeInsets.fromLTRB(20, 4, 20, 32),
              children: [
                Text(nombre == null ? 'Panel de administración' : 'Hola, $nombre', style: t.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
                const SizedBox(height: 4),
                Text('Gestiona las cuentas de pastores y hermanos, y las iglesias.',
                    style: t.bodyMedium?.copyWith(color: AppColors.textoSecundario)),
                const SizedBox(height: 16),
                Tarjeta(
                  child: Row(
                    children: [
                      Expanded(child: _Cifra(valor: '${r.total}', titulo: 'cuentas')),
                      Expanded(child: _Cifra(valor: '${r.bloqueados}', titulo: 'bloqueadas', color: r.bloqueados > 0 ? AppColors.rojo : null)),
                    ],
                  ),
                ),
                const TituloSeccion('Cuentas activas por rol'),
                for (final rol in RolUsuario.values) ...[
                  Tarjeta(
                    child: Row(
                      children: [
                        Icon(_icono(rol), color: AppColors.primario),
                        const SizedBox(width: 14),
                        Expanded(child: Text(rol.plural, style: t.titleSmall)),
                        Text('${r.activosPorRol[rol] ?? 0}', style: t.titleLarge?.copyWith(fontWeight: FontWeight.w800)),
                      ],
                    ),
                  ),
                  const SizedBox(height: 8),
                ],
                const SizedBox(height: 8),
                Tarjeta(
                  color: AppColors.terracotaTinte,
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Icon(Icons.shield_outlined, color: AppColors.terracota),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          'Privacidad: como administrador no ves montos, movimientos ni reportes de nadie. Solo gestionas las cuentas.',
                          style: t.bodySmall?.copyWith(color: AppColors.terracotaOscuro),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );
  }

  IconData _icono(RolUsuario rol) => switch (rol) {
        RolUsuario.admin => Icons.admin_panel_settings_outlined,
        RolUsuario.pastor => Icons.record_voice_over_outlined,
        RolUsuario.hermano => Icons.person_outline,
      };
}

class _Cifra extends StatelessWidget {
  const _Cifra({required this.valor, required this.titulo, this.color});

  final String valor;
  final String titulo;
  final Color? color;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Column(
      children: [
        Text(valor, style: t.displaySmall?.copyWith(fontWeight: FontWeight.w800, color: color ?? AppColors.primario)),
        Text(titulo, style: t.labelMedium?.copyWith(color: AppColors.textoSecundario)),
      ],
    );
  }
}
