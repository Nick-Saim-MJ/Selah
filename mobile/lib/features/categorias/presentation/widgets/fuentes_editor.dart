import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/entrada.dart';
import '../../../../core/utils/formato.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../domain/entities/categorias_entities.dart';
import '../cubit/categorias_cubits.dart';

/// Fuentes de ingreso con su monto mensual estimado (asistente inicial, paso 2, y Configuración).
class FuentesEditor extends StatelessWidget {
  const FuentesEditor({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<FuentesIngresoCubit, AsyncState<List<FuenteIngreso>>>(
      builder: (context, state) => AsyncView<List<FuenteIngreso>>(
        state: state,
        onRetry: context.read<FuentesIngresoCubit>().cargar,
        builder: (context, fuentes) {
          final total = fuentes.fold<double>(0, (a, f) => a + f.montoEstimadoMensual);
          return Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (fuentes.isEmpty)
                const Tarjeta(child: Text('Aún no agregaste ninguna fuente. Por ejemplo: Salario, Negocio, Pensión.'))
              else
                Tarjeta(
                  padding: EdgeInsets.zero,
                  child: Column(
                    children: [
                      for (final f in fuentes)
                        ListTile(
                          title: Text(f.nombre),
                          subtitle: Text('${Formato.moneda(f.montoEstimadoMensual)} al mes'),
                          trailing: IconButton(
                            tooltip: 'Quitar',
                            icon: const Icon(Icons.delete_outline),
                            onPressed: () => _quitar(context, f),
                          ),
                          onTap: () => _editar(context, f),
                        ),
                    ],
                  ),
                ),
              const SizedBox(height: 8),
              Text('Ingreso mensual estimado: ${Formato.moneda(total)}',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.textoSecundario)),
              const SizedBox(height: 12),
              OutlinedButton.icon(
                onPressed: () => _editar(context, const FuenteIngreso(id: '', nombre: '', montoEstimadoMensual: 0)),
                icon: const Icon(Icons.add),
                label: const Text('Agregar fuente de ingreso'),
              ),
            ],
          );
        },
      ),
    );
  }

  Future<void> _quitar(BuildContext context, FuenteIngreso f) async {
    final cubit = context.read<FuentesIngresoCubit>();
    if (!await confirmar(context, titulo: 'Quitar «${f.nombre}»', mensaje: 'Dejará de contar en tu ingreso estimado.', aceptar: 'Quitar')) {
      return;
    }
    final error = await cubit.ocultar(f.id);
    if (context.mounted && error != null) avisar(context, error, error: true);
  }

  Future<void> _editar(BuildContext context, FuenteIngreso f) async {
    final cubit = context.read<FuentesIngresoCubit>();
    final nueva = await showModalBottomSheet<FuenteIngreso>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (_) => _FormFuente(inicial: f),
    );
    if (nueva == null || !context.mounted) return;
    final error = await cubit.guardar(nueva);
    if (context.mounted && error != null) avisar(context, error, error: true);
  }
}

class _FormFuente extends StatefulWidget {
  const _FormFuente({required this.inicial});

  final FuenteIngreso inicial;

  @override
  State<_FormFuente> createState() => _FormFuenteState();
}

class _FormFuenteState extends State<_FormFuente> {
  late final _nombre = TextEditingController(text: widget.inicial.nombre);
  late final _monto = TextEditingController(
      text: widget.inicial.montoEstimadoMensual > 0 ? montoParaCampo(widget.inicial.montoEstimadoMensual) : '');
  final _form = GlobalKey<FormState>();

  @override
  void dispose() {
    _nombre.dispose();
    _monto.dispose();
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
            Text(nueva ? 'Nueva fuente de ingreso' : 'Editar fuente', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 16),
            TextFormField(
              controller: _nombre,
              autofocus: nueva,
              textCapitalization: TextCapitalization.sentences,
              decoration: const InputDecoration(labelText: 'Nombre (Salario, Negocio…)'),
              validator: (v) => (v == null || v.trim().isEmpty) ? 'Ponle un nombre' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _monto,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              inputFormatters: [formatoMonto],
              decoration: const InputDecoration(labelText: 'Monto estimado mensual', prefixText: 'S/ '),
              validator: (v) => parseMonto(v ?? '') == null ? 'Monto inválido' : null,
            ),
            const SizedBox(height: 20),
            FilledButton(
              onPressed: () {
                if (!_form.currentState!.validate()) return;
                Navigator.pop(
                  context,
                  FuenteIngreso(
                    id: widget.inicial.id,
                    nombre: _nombre.text.trim(),
                    montoEstimadoMensual: parseMonto(_monto.text) ?? 0,
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
