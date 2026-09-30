import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../../iglesias/domain/entities/iglesia.dart';
import '../../../iglesias/presentation/cubit/iglesias_cubit.dart';

/// Alta y edición de iglesias. Una iglesia desactivada deja de aparecer en el registro.
class AdminIglesiasPage extends StatelessWidget {
  const AdminIglesiasPage({super.key});

  Future<void> _editar(BuildContext context, Iglesia inicial) async {
    final cubit = context.read<IglesiasCubit>();
    final resultado = await showModalBottomSheet<Iglesia>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (_) => _FormIglesia(inicial: inicial),
    );
    if (resultado == null || !context.mounted) return;
    final error = await cubit.guardar(resultado);
    if (context.mounted) avisar(context, error ?? (inicial.id.isEmpty ? 'Iglesia creada' : 'Iglesia actualizada'), error: error != null);
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Iglesias')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _editar(context, const Iglesia(id: '', nombre: '')),
        icon: const Icon(Icons.add),
        label: const Text('Nueva iglesia'),
      ),
      body: BlocBuilder<IglesiasCubit, AsyncState<List<Iglesia>>>(
        builder: (context, state) => AsyncView<List<Iglesia>>(
          state: state,
          onRetry: context.read<IglesiasCubit>().cargar,
          onRefresh: context.read<IglesiasCubit>().cargar,
          builder: (context, iglesias) => ListView(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 96),
            children: [
              if (iglesias.isEmpty) const Tarjeta(child: Text('Aún no hay iglesias. Crea la primera para que los hermanos puedan registrarse.')),
              for (final i in iglesias) ...[
                Tarjeta(
                  onTap: () => _editar(context, i),
                  child: Row(
                    children: [
                      const Icon(Icons.church_outlined, color: AppColors.primario),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(i.nombre, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
                            Text([if (i.ciudad != null) i.ciudad!, if (i.distrito != null) i.distrito!].join(' · '),
                                style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
                          ],
                        ),
                      ),
                      Etiqueta(i.activa ? 'Activa' : 'Inactiva', color: i.activa ? AppColors.verde : AppColors.textoTenue),
                    ],
                  ),
                ),
                const SizedBox(height: 8),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

class _FormIglesia extends StatefulWidget {
  const _FormIglesia({required this.inicial});

  final Iglesia inicial;

  @override
  State<_FormIglesia> createState() => _FormIglesiaState();
}

class _FormIglesiaState extends State<_FormIglesia> {
  final _form = GlobalKey<FormState>();
  late final _nombre = TextEditingController(text: widget.inicial.nombre);
  late final _ciudad = TextEditingController(text: widget.inicial.ciudad ?? '');
  late final _distrito = TextEditingController(text: widget.inicial.distrito ?? '');
  late bool _activa = widget.inicial.activa;

  @override
  void dispose() {
    _nombre.dispose();
    _ciudad.dispose();
    _distrito.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final nueva = widget.inicial.id.isEmpty;
    return Padding(
      padding: EdgeInsets.fromLTRB(20, 0, 20, 20 + MediaQuery.viewInsetsOf(context).bottom),
      child: Form(
        key: _form,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(nueva ? 'Nueva iglesia' : 'Editar iglesia', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 16),
            TextFormField(
              controller: _nombre,
              autofocus: nueva,
              textCapitalization: TextCapitalization.words,
              decoration: const InputDecoration(labelText: 'Nombre'),
              validator: (v) => (v == null || v.trim().isEmpty) ? 'Ponle un nombre' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(controller: _ciudad, textCapitalization: TextCapitalization.words, decoration: const InputDecoration(labelText: 'Ciudad')),
            const SizedBox(height: 12),
            TextFormField(controller: _distrito, textCapitalization: TextCapitalization.words, decoration: const InputDecoration(labelText: 'Distrito')),
            if (!nueva)
              SwitchListTile(
                contentPadding: EdgeInsets.zero,
                title: const Text('Activa'),
                subtitle: const Text('Si la desactivas, deja de aparecer en el registro'),
                value: _activa,
                onChanged: (v) => setState(() => _activa = v),
              ),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: () {
                if (!_form.currentState!.validate()) return;
                Navigator.pop(
                  context,
                  Iglesia(
                    id: widget.inicial.id,
                    nombre: _nombre.text.trim(),
                    ciudad: _ciudad.text.trim().isEmpty ? null : _ciudad.text.trim(),
                    distrito: _distrito.text.trim().isEmpty ? null : _distrito.text.trim(),
                    activa: _activa,
                  ),
                );
              },
              child: const Text('Guardar'),
            ),
          ],
        ),
      ),
    );
  }
}
