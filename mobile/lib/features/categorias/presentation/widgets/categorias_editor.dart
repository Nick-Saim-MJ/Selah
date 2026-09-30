import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/colores.dart';
import '../../../../core/utils/entrada.dart';
import '../../../../core/utils/formato.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../domain/entities/categorias_entities.dart';
import '../cubit/categorias_cubits.dart';

/// Lista editable de categorías de gasto con su presupuesto. Se usa en Configuración y en el asistente inicial.
class CategoriasEditor extends StatelessWidget {
  const CategoriasEditor({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<CategoriasCubit, AsyncState<List<CategoriaGasto>>>(
      builder: (context, state) => AsyncView<List<CategoriaGasto>>(
        state: state,
        onRetry: context.read<CategoriasCubit>().cargar,
        builder: (context, categorias) {
          final total = categorias.fold<double>(0, (a, c) => a + c.presupuestoMensual);
          return Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Tarjeta(
                padding: EdgeInsets.zero,
                child: Column(
                  children: [
                    for (final c in categorias)
                      ListTile(
                        leading: CircleAvatar(radius: 12, backgroundColor: colorDeHex(c.color)),
                        title: Text(c.nombre),
                        subtitle: Text(c.presupuestoMensual > 0
                            ? 'Presupuesto ${Formato.moneda(c.presupuestoMensual)} al mes'
                            : 'Sin presupuesto'),
                        trailing: IconButton(
                          tooltip: 'Quitar',
                          icon: const Icon(Icons.delete_outline),
                          onPressed: () => _quitar(context, c),
                        ),
                        onTap: () => _editar(context, c),
                      ),
                  ],
                ),
              ),
              const SizedBox(height: 8),
              Text('Presupuesto mensual total: ${Formato.moneda(total)}',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.textoSecundario)),
              const SizedBox(height: 12),
              OutlinedButton.icon(
                onPressed: () => _editar(context, const CategoriaGasto(id: '', nombre: '', presupuestoMensual: 0)),
                icon: const Icon(Icons.add),
                label: const Text('Agregar categoría'),
              ),
            ],
          );
        },
      ),
    );
  }

  Future<void> _quitar(BuildContext context, CategoriaGasto c) async {
    final cubit = context.read<CategoriasCubit>();
    final si = await confirmar(context,
        titulo: 'Quitar «${c.nombre}»',
        mensaje: 'Dejará de aparecer para nuevos gastos. Los movimientos anteriores se conservan.',
        aceptar: 'Quitar');
    if (!si || !context.mounted) return;
    final error = await cubit.ocultar(c.id);
    if (context.mounted && error != null) avisar(context, error, error: true);
  }

  Future<void> _editar(BuildContext context, CategoriaGasto c) async {
    final cubit = context.read<CategoriasCubit>();
    final nueva = await showModalBottomSheet<CategoriaGasto>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (_) => _FormCategoria(inicial: c),
    );
    if (nueva == null || !context.mounted) return;
    final error = await cubit.guardar(nueva);
    if (context.mounted && error != null) avisar(context, error, error: true);
  }
}

class _FormCategoria extends StatefulWidget {
  const _FormCategoria({required this.inicial});

  final CategoriaGasto inicial;

  @override
  State<_FormCategoria> createState() => _FormCategoriaState();
}

class _FormCategoriaState extends State<_FormCategoria> {
  late final _nombre = TextEditingController(text: widget.inicial.nombre);
  late final _presupuesto = TextEditingController(
      text: widget.inicial.presupuestoMensual > 0 ? montoParaCampo(widget.inicial.presupuestoMensual) : '');
  late String _color = widget.inicial.color ?? paletaCategorias.first;
  final _form = GlobalKey<FormState>();

  @override
  void dispose() {
    _nombre.dispose();
    _presupuesto.dispose();
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
            Text(nueva ? 'Nueva categoría' : 'Editar categoría', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 16),
            TextFormField(
              controller: _nombre,
              autofocus: nueva,
              textCapitalization: TextCapitalization.sentences,
              decoration: const InputDecoration(labelText: 'Nombre'),
              validator: (v) => (v == null || v.trim().isEmpty) ? 'Ponle un nombre' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _presupuesto,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              inputFormatters: [formatoMonto],
              decoration: const InputDecoration(labelText: 'Presupuesto mensual', prefixText: 'S/ ', hintText: '0'),
              validator: (v) => parseMonto(v ?? '') == null ? 'Monto inválido' : null,
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: [
                for (final hex in paletaCategorias)
                  GestureDetector(
                    onTap: () => setState(() => _color = hex),
                    child: CircleAvatar(
                      radius: 16,
                      backgroundColor: colorDeHex(hex),
                      child: _color == hex ? const Icon(Icons.check, size: 18, color: Colors.white) : null,
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 20),
            FilledButton(
              onPressed: () {
                if (!_form.currentState!.validate()) return;
                Navigator.pop(
                  context,
                  CategoriaGasto(
                    id: widget.inicial.id,
                    nombre: _nombre.text.trim(),
                    presupuestoMensual: parseMonto(_presupuesto.text) ?? 0,
                    color: _color,
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
