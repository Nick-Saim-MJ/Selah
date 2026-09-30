import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/bloc/accion_cubit.dart';
import '../../../../core/bloc/async_state.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/usecase/usecase.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../auth/domain/entities/perfil.dart';
import '../../../auth/presentation/bloc/auth_bloc.dart';
import '../../../categorias/presentation/widgets/categorias_editor.dart';
import '../../../categorias/presentation/widgets/fuentes_editor.dart';
import '../../../mayordomia/domain/entities/mayordomia_entities.dart';
import '../../../mayordomia/presentation/cubit/mayordomia_cubits.dart';
import '../../../mayordomia/presentation/widgets/config_mayordomia_form.dart';

/// Asistente de configuración inicial, 4 pasos (spec 4.2): planificar antes de actuar (Lucas 14:28).
/// Se muestra una sola vez, hasta terminarlo; todo se puede cambiar después desde Configuración.
class AsistentePage extends StatefulWidget {
  const AsistentePage({super.key});

  @override
  State<AsistentePage> createState() => _AsistentePageState();
}

class _AsistentePageState extends State<AsistentePage> {
  static const _titulos = ['Bienvenido', 'Tus ingresos', 'Tu mayordomía', 'Tus gastos'];

  int _paso = 0;
  ConfigMayordomia? _config;
  bool _trabajando = false;

  Future<void> _siguiente() async {
    if (_paso == 2 && _config != null) {
      setState(() => _trabajando = true);
      final error = await context.read<ConfigMayordomiaCubit>().guardar(_config!);
      if (!mounted) return;
      setState(() => _trabajando = false);
      if (error != null) {
        avisar(context, error, error: true);
        return;
      }
    }
    if (_paso < _titulos.length - 1) {
      setState(() => _paso++);
      return;
    }
    final accion = context.read<AccionCubit<SinParametros, Perfil>>();
    final bien = await accion.ejecutar(const SinParametros());
    if (!mounted) return;
    if (bien) {
      // El router lleva a Inicio en cuanto la sesión refleja que el asistente terminó
      context.read<AuthBloc>().add(const AuthSesionRefrescada());
    } else {
      avisar(context, accion.state.error ?? 'No se pudo terminar', error: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final ultimo = _paso == _titulos.length - 1;
    return Scaffold(
      appBar: AppBar(
        title: Text('Paso ${_paso + 1} de ${_titulos.length}'),
        automaticallyImplyLeading: false,
        actions: [
          TextButton(
            onPressed: () => context.read<AuthBloc>().add(const AuthCierreSolicitado()),
            child: const Text('Salir'),
          ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: LinearProgressIndicator(
                value: (_paso + 1) / _titulos.length,
                minHeight: 6,
                borderRadius: BorderRadius.circular(6),
                color: AppColors.primario,
                backgroundColor: AppColors.chip,
              ),
            ),
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text(_titulos[_paso], style: t.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
                    const SizedBox(height: 8),
                    ..._contenido(t),
                  ],
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 0, 20, 16),
              child: Row(
                children: [
                  if (_paso > 0)
                    TextButton(onPressed: _trabajando ? null : () => setState(() => _paso--), child: const Text('Atrás')),
                  const Spacer(),
                  BlocBuilder<AccionCubit<SinParametros, Perfil>, AccionState<Perfil>>(
                    builder: (context, s) => FilledButton(
                      onPressed: _trabajando || s.enviando ? null : _siguiente,
                      style: FilledButton.styleFrom(minimumSize: const Size(160, 52)),
                      child: (_trabajando || s.enviando)
                          ? const SizedBox.square(dimension: 22, child: CircularProgressIndicator(strokeWidth: 2.5))
                          : Text(ultimo ? 'Comenzar' : 'Siguiente'),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  List<Widget> _contenido(TextTheme t) => switch (_paso) {
        0 => [
            Text(
              'Antes de registrar dinero, definamos tu plan. Así, desde el primer día, tu diezmo se aparta primero y tu saldo disponible es real.',
              style: t.bodyLarge?.copyWith(color: AppColors.textoSecundario),
            ),
            const SizedBox(height: 20),
            const _PuntoBienvenida(Icons.payments_outlined, 'Cuéntanos cuánto ingresa cada mes.'),
            const _PuntoBienvenida(Icons.volunteer_activism_outlined, 'Elige cuánto apartas para Dios.'),
            const _PuntoBienvenida(Icons.category_outlined, 'Ajusta tus categorías y presupuestos.'),
            const SizedBox(height: 20),
            Text('¿Quién de vosotros, queriendo edificar una torre, no se sienta primero y calcula los gastos?\nLucas 14:28',
                style: t.bodySmall?.copyWith(color: AppColors.terracota, fontStyle: FontStyle.italic)),
          ],
        1 => [
            Text('Agrega tus fuentes de ingreso con un monto mensual aproximado. Puedes cambiarlas cuando quieras.',
                style: t.bodyMedium?.copyWith(color: AppColors.textoSecundario)),
            const SizedBox(height: 16),
            const FuentesEditor(),
          ],
        2 => [
            Text('Lo primero es para Dios. Este porcentaje se aparta de cada ingreso antes de que veas tu saldo disponible.',
                style: t.bodyMedium?.copyWith(color: AppColors.textoSecundario)),
            const SizedBox(height: 16),
            BlocBuilder<ConfigMayordomiaCubit, AsyncState<ConfigMayordomia>>(
              builder: (context, state) => AsyncView<ConfigMayordomia>(
                state: state,
                onRetry: context.read<ConfigMayordomiaCubit>().cargar,
                builder: (context, config) => ConfigMayordomiaForm(inicial: config, onCambio: (c) => _config = c),
              ),
            ),
          ],
        _ => [
            Text('Estas son las categorías sugeridas. Quita las que no uses, agrega otras y pon un presupuesto mensual a cada una.',
                style: t.bodyMedium?.copyWith(color: AppColors.textoSecundario)),
            const SizedBox(height: 16),
            const CategoriasEditor(),
          ],
      };
}

class _PuntoBienvenida extends StatelessWidget {
  const _PuntoBienvenida(this.icono, this.texto);

  final IconData icono;
  final String texto;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          CircleAvatar(backgroundColor: AppColors.terracotaTinte, child: Icon(icono, color: AppColors.terracota)),
          const SizedBox(width: 14),
          Expanded(child: Text(texto, style: Theme.of(context).textTheme.bodyLarge)),
        ],
      ),
    );
  }
}
