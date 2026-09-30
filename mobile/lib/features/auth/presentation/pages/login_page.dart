import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../iglesias/domain/entities/iglesia.dart';
import '../../../iglesias/presentation/cubit/iglesias_cubit.dart';
import '../bloc/auth_bloc.dart';

/// Inicio de sesión y auto-registro de un hermano (elige su iglesia) en una sola pantalla.
class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _form = GlobalKey<FormState>();
  final _email = TextEditingController();
  final _password = TextEditingController();
  final _nombres = TextEditingController();
  final _apellidos = TextEditingController();
  String? _iglesiaId;
  bool _modoRegistro = false;
  bool _verPassword = false;

  @override
  void dispose() {
    _email.dispose();
    _password.dispose();
    _nombres.dispose();
    _apellidos.dispose();
    super.dispose();
  }

  void _enviar() {
    if (!_form.currentState!.validate()) return;
    final bloc = context.read<AuthBloc>();
    bloc.add(_modoRegistro
        ? AuthRegistroSolicitado(
            email: _email.text,
            password: _password.text,
            nombres: _nombres.text,
            apellidos: _apellidos.text,
            iglesiaId: _iglesiaId!,
          )
        : AuthLoginSolicitado(email: _email.text, password: _password.text));
  }

  @override
  Widget build(BuildContext context) {
    final texto = Theme.of(context).textTheme;
    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Form(
                key: _form,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text('SelahFinance',
                        style: texto.headlineMedium?.copyWith(fontWeight: FontWeight.w800, color: AppColors.primario)),
                    const SizedBox(height: 4),
                    Text('Administra con propósito, vive con libertad.',
                        style: texto.bodyMedium?.copyWith(color: AppColors.textoSecundario)),
                    const SizedBox(height: 32),
                    if (_modoRegistro) ...[
                      TextFormField(
                        controller: _nombres,
                        decoration: const InputDecoration(labelText: 'Nombre'),
                        textCapitalization: TextCapitalization.words,
                        validator: (v) => (v == null || v.trim().isEmpty) ? 'Ingresa tu nombre' : null,
                      ),
                      const SizedBox(height: 12),
                      TextFormField(
                        controller: _apellidos,
                        decoration: const InputDecoration(labelText: 'Apellidos (opcional)'),
                        textCapitalization: TextCapitalization.words,
                      ),
                      const SizedBox(height: 12),
                      _SelectorIglesia(valor: _iglesiaId, onCambio: (v) => setState(() => _iglesiaId = v)),
                      const SizedBox(height: 12),
                    ],
                    TextFormField(
                      controller: _email,
                      decoration: const InputDecoration(labelText: 'Email'),
                      keyboardType: TextInputType.emailAddress,
                      autofillHints: const [AutofillHints.email],
                      validator: (v) => (v == null || !v.contains('@')) ? 'Email inválido' : null,
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _password,
                      decoration: InputDecoration(
                        labelText: 'Contraseña',
                        suffixIcon: IconButton(
                          icon: Icon(_verPassword ? Icons.visibility_off_outlined : Icons.visibility_outlined),
                          onPressed: () => setState(() => _verPassword = !_verPassword),
                        ),
                      ),
                      obscureText: !_verPassword,
                      autofillHints: const [AutofillHints.password],
                      onFieldSubmitted: (_) => _enviar(),
                      validator: (v) => (v == null || v.length < 8) ? 'Mínimo 8 caracteres' : null,
                    ),
                    const SizedBox(height: 24),
                    BlocBuilder<AuthBloc, AuthState>(
                      builder: (context, state) {
                        final cargando = state is AuthCargando;
                        final error = state is AuthNoAutenticado ? state.error : null;
                        return Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            if (error != null) ...[
                              Text(error, style: texto.bodyMedium?.copyWith(color: AppColors.rojo)),
                              const SizedBox(height: 12),
                            ],
                            FilledButton(
                              onPressed: cargando ? null : _enviar,
                              child: cargando
                                  ? const SizedBox.square(
                                      dimension: 22, child: CircularProgressIndicator(strokeWidth: 2.5))
                                  : Text(_modoRegistro ? 'Crear cuenta' : 'Ingresar'),
                            ),
                          ],
                        );
                      },
                    ),
                    TextButton(
                      onPressed: () => setState(() => _modoRegistro = !_modoRegistro),
                      child: Text(_modoRegistro ? 'Ya tengo cuenta' : 'Soy nuevo: crear una cuenta'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _SelectorIglesia extends StatelessWidget {
  const _SelectorIglesia({required this.valor, required this.onCambio});

  final String? valor;
  final ValueChanged<String?> onCambio;

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<IglesiasCubit, AsyncState<List<Iglesia>>>(
      builder: (context, state) {
        final iglesias = state.datos;
        if (iglesias == null) {
          return state.estado == Estado.fallo
              ? OutlinedButton.icon(
                  onPressed: context.read<IglesiasCubit>().cargar,
                  icon: const Icon(Icons.refresh),
                  label: const Text('No se pudo cargar las iglesias. Reintentar'),
                )
              : const LinearProgressIndicator();
        }
        return DropdownButtonFormField<String>(
          initialValue: valor,
          isExpanded: true,
          decoration: const InputDecoration(labelText: 'Mi iglesia'),
          items: [for (final i in iglesias) DropdownMenuItem(value: i.id, child: Text(i.etiqueta, overflow: TextOverflow.ellipsis))],
          onChanged: onCambio,
          validator: (v) => v == null ? 'Elige tu iglesia' : null,
        );
      },
    );
  }
}
