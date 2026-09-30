import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/bloc/accion_cubit.dart';
import '../../../../core/di/injection_container.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/widgets/async_view.dart';
import '../../domain/usecases/auth_usecases.dart';
import '../bloc/auth_bloc.dart';

/// Cambio de contraseña. Con `obligatorio` (cuenta con contraseña temporal) no se puede salir sin cambiarla.
class CambiarPasswordPage extends StatelessWidget {
  const CambiarPasswordPage({super.key, this.obligatorio = false});

  final bool obligatorio;

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => AccionCubit<CambioPasswordParams, void>(sl<CambiarPassword>()),
      child: _Formulario(obligatorio: obligatorio),
    );
  }
}

class _Formulario extends StatefulWidget {
  const _Formulario({required this.obligatorio});

  final bool obligatorio;

  @override
  State<_Formulario> createState() => _FormularioState();
}

class _FormularioState extends State<_Formulario> {
  final _form = GlobalKey<FormState>();
  final _actual = TextEditingController();
  final _nueva = TextEditingController();
  final _repetir = TextEditingController();

  @override
  void dispose() {
    _actual.dispose();
    _nueva.dispose();
    _repetir.dispose();
    super.dispose();
  }

  Future<void> _guardar() async {
    if (!_form.currentState!.validate()) return;
    final cubit = context.read<AccionCubit<CambioPasswordParams, void>>();
    final bien = await cubit.ejecutar(CambioPasswordParams(actual: _actual.text, nueva: _nueva.text));
    if (!mounted) return;
    if (!bien) {
      avisar(context, cubit.state.error ?? 'No se pudo cambiar la contraseña', error: true);
      return;
    }
    avisar(context, 'Contraseña actualizada');
    // El router decide a dónde ir cuando la sesión deja de tener la marca de contraseña temporal
    context.read<AuthBloc>().add(const AuthSesionRefrescada());
    if (!widget.obligatorio && Navigator.of(context).canPop()) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final texto = Theme.of(context).textTheme;
    return PopScope(
      canPop: !widget.obligatorio,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Cambiar contraseña'),
          automaticallyImplyLeading: !widget.obligatorio,
          actions: [
            if (widget.obligatorio)
              TextButton(
                onPressed: () => context.read<AuthBloc>().add(const AuthCierreSolicitado()),
                child: const Text('Salir'),
              ),
          ],
        ),
        body: SafeArea(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: Form(
              key: _form,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  if (widget.obligatorio)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 16),
                      child: Text(
                        'Tu cuenta fue creada con una contraseña temporal. Elige una propia para continuar.',
                        style: texto.bodyMedium?.copyWith(color: AppColors.textoSecundario),
                      ),
                    ),
                  TextFormField(
                    controller: _actual,
                    obscureText: true,
                    decoration: InputDecoration(labelText: widget.obligatorio ? 'Contraseña temporal' : 'Contraseña actual'),
                    validator: (v) => (v == null || v.isEmpty) ? 'Ingresa tu contraseña actual' : null,
                  ),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: _nueva,
                    obscureText: true,
                    decoration: const InputDecoration(labelText: 'Contraseña nueva', helperText: 'Mínimo 8 caracteres'),
                    validator: (v) => (v == null || v.length < 8) ? 'Mínimo 8 caracteres' : null,
                  ),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: _repetir,
                    obscureText: true,
                    decoration: const InputDecoration(labelText: 'Repite la contraseña nueva'),
                    validator: (v) => v != _nueva.text ? 'Las contraseñas no coinciden' : null,
                  ),
                  const SizedBox(height: 24),
                  BlocBuilder<AccionCubit<CambioPasswordParams, void>, AccionState<void>>(
                    builder: (context, s) => FilledButton(
                      onPressed: s.enviando ? null : _guardar,
                      child: s.enviando
                          ? const SizedBox.square(dimension: 22, child: CircularProgressIndicator(strokeWidth: 2.5))
                          : const Text('Guardar contraseña'),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
