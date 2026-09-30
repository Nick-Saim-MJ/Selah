import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/router/rutas.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/entrada.dart';
import '../../../../core/utils/formato.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../../movimientos/domain/entities/tipo_movimiento.dart';
import '../../domain/entities/metas_entities.dart';
import '../cubit/metas_cubits.dart';

/// Metas: una sola pestaña para lo "comprometido a futuro": ahorros y deudas (spec 4.6).
class MetasPage extends StatefulWidget {
  const MetasPage({super.key});

  @override
  State<MetasPage> createState() => _MetasPageState();
}

class _MetasPageState extends State<MetasPage> {
  bool _verDeudas = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Metas')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 8),
            child: SizedBox(
              width: double.infinity,
              child: SegmentedButton<bool>(
                segments: const [
                  ButtonSegment(value: false, label: Text('Ahorros'), icon: Icon(Icons.savings_outlined)),
                  ButtonSegment(value: true, label: Text('Deudas'), icon: Icon(Icons.credit_card_outlined)),
                ],
                selected: {_verDeudas},
                onSelectionChanged: (s) => setState(() => _verDeudas = s.first),
              ),
            ),
          ),
          Expanded(child: _verDeudas ? const _VistaDeudas() : const _VistaAhorros()),
        ],
      ),
    );
  }
}

// ------------------------------------------------------------------ Ahorros

class _VistaAhorros extends StatelessWidget {
  const _VistaAhorros();

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<MetasCubit, AsyncState<List<MetaAhorro>>>(
      builder: (context, state) => AsyncView<List<MetaAhorro>>(
        state: state,
        onRetry: context.read<MetasCubit>().cargar,
        onRefresh: context.read<MetasCubit>().cargar,
        builder: (context, metas) => ListView(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
          children: [
            if (metas.isEmpty)
              const Tarjeta(
                child: Text('Aún no tienes metas. Ahorrar con un propósito claro te ayuda a no gastar el dinero sin pensar.'),
              ),
            for (final m in metas) ...[_TarjetaMeta(meta: m), const SizedBox(height: 12)],
            OutlinedButton.icon(
              onPressed: () => _nuevaMeta(context),
              icon: const Icon(Icons.add),
              label: const Text('Nueva meta de ahorro'),
            ),
            const SizedBox(height: 20),
            Center(
              child: Text('Ve a la hormiga, oh perezoso, mira sus caminos, y sé sabio.\nProverbios 6:6',
                  textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.terracota, fontStyle: FontStyle.italic)),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _nuevaMeta(BuildContext context) async {
    final cubit = context.read<MetasCubit>();
    final meta = await showModalBottomSheet<MetaAhorro>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (_) => const _FormMeta(),
    );
    if (meta == null || !context.mounted) return;
    final error = await cubit.guardar(meta);
    if (context.mounted) avisar(context, error ?? 'Meta creada', error: error != null);
  }
}

class _TarjetaMeta extends StatelessWidget {
  const _TarjetaMeta({required this.meta});

  final MetaAhorro meta;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final pct = meta.porcentaje / 100;
    return Tarjeta(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              SizedBox(
                width: 64,
                height: 64,
                child: Stack(
                  alignment: Alignment.center,
                  children: [
                    CircularProgressIndicator(
                      value: pct.clamp(0.0, 1.0),
                      strokeWidth: 7,
                      backgroundColor: AppColors.chip,
                      color: meta.completada ? AppColors.verde : AppColors.primario,
                    ),
                    Text(Formato.porcentaje(meta.porcentaje), style: t.labelLarge?.copyWith(fontWeight: FontWeight.w700)),
                  ],
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Flexible(child: Text(meta.nombre, style: t.titleMedium?.copyWith(fontWeight: FontWeight.w700))),
                        if (meta.esPrincipal) ...[const SizedBox(width: 8), const Etiqueta('Principal', color: AppColors.terracota)],
                        if (meta.completada) ...[const SizedBox(width: 8), const Etiqueta('¡Lograda!', color: AppColors.verde)],
                      ],
                    ),
                    const SizedBox(height: 2),
                    Text(meta.proposito, style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
                    const SizedBox(height: 8),
                    Text('${Formato.moneda(meta.montoActual)} de ${Formato.moneda(meta.montoObjetivo)}',
                        style: t.bodyMedium?.copyWith(fontWeight: FontWeight.w600)),
                    if (meta.fechaObjetivo != null)
                      Text('Para ${Formato.mesLargo(Formato.periodo(meta.fechaObjetivo!))}',
                          style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
                    if (meta.aporteMensualSugerido != null && !meta.completada)
                      Text('Sugerido: ${Formato.moneda(meta.aporteMensualSugerido!)} al mes',
                          style: t.bodySmall?.copyWith(color: AppColors.primario)),
                  ],
                ),
              ),
              PopupMenuButton<String>(
                onSelected: (v) => _accion(context, v),
                itemBuilder: (_) => [
                  if (!meta.esPrincipal && !meta.completada) const PopupMenuItem(value: 'principal', child: Text('Hacer principal')),
                  const PopupMenuItem(value: 'cancelar', child: Text('Cancelar meta')),
                ],
              ),
            ],
          ),
          if (!meta.completada) ...[
            const SizedBox(height: 12),
            Align(
              alignment: Alignment.centerRight,
              child: FilledButton.tonalIcon(
                // Aportar no duplica el formulario: abre Movimientos con el tipo y la meta ya elegidos
                onPressed: () async {
                  final cubit = context.read<MetasCubit>();
                  await context.push<bool>(Rutas.nuevoMovimiento(TipoMovimiento.aporteMeta, metaId: meta.id));
                  cubit.cargar();
                },
                style: FilledButton.styleFrom(minimumSize: const Size(0, 44)),
                icon: const Icon(Icons.add),
                label: const Text('Aportar'),
              ),
            ),
          ],
        ],
      ),
    );
  }

  Future<void> _accion(BuildContext context, String accion) async {
    final cubit = context.read<MetasCubit>();
    if (accion == 'cancelar' &&
        !await confirmar(context,
            titulo: 'Cancelar «${meta.nombre}»',
            mensaje: 'La meta dejará de mostrarse. Lo que aportaste sigue en tus movimientos.',
            aceptar: 'Cancelar meta')) {
      return;
    }
    final error = accion == 'principal' ? await cubit.hacerPrincipal(meta.id) : await cubit.cancelar(meta.id);
    if (context.mounted && error != null) avisar(context, error, error: true);
  }
}

class _FormMeta extends StatefulWidget {
  const _FormMeta();

  @override
  State<_FormMeta> createState() => _FormMetaState();
}

class _FormMetaState extends State<_FormMeta> {
  final _form = GlobalKey<FormState>();
  final _nombre = TextEditingController();
  final _proposito = TextEditingController();
  final _monto = TextEditingController();
  TipoMeta _tipo = TipoMeta.especifica;
  DateTime? _fecha;

  @override
  void dispose() {
    _nombre.dispose();
    _proposito.dispose();
    _monto.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.fromLTRB(20, 0, 20, 20 + MediaQuery.viewInsetsOf(context).bottom),
      child: SingleChildScrollView(
        child: Form(
          key: _form,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text('Nueva meta de ahorro', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 16),
              TextFormField(
                controller: _nombre,
                autofocus: true,
                textCapitalization: TextCapitalization.sentences,
                decoration: const InputDecoration(labelText: 'Nombre (Fondo de emergencia…)'),
                validator: (v) => (v == null || v.trim().isEmpty) ? 'Ponle un nombre' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _proposito,
                textCapitalization: TextCapitalization.sentences,
                decoration: const InputDecoration(labelText: '¿Para qué ahorras?', helperText: 'Cada meta tiene un propósito'),
                validator: (v) => (v == null || v.trim().isEmpty) ? 'Cuéntanos el propósito' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _monto,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                inputFormatters: [formatoMonto],
                decoration: const InputDecoration(labelText: 'Monto objetivo', prefixText: 'S/ '),
                validator: (v) {
                  final m = parseMonto(v ?? '');
                  return (m == null || m <= 0) ? 'Indica un monto mayor que cero' : null;
                },
              ),
              const SizedBox(height: 12),
              Wrap(
                spacing: 8,
                children: [
                  for (final t in TipoMeta.values)
                    ChoiceChip(label: Text(t.etiqueta), selected: _tipo == t, onSelected: (_) => setState(() => _tipo = t)),
                ],
              ),
              ListTile(
                contentPadding: EdgeInsets.zero,
                leading: const Icon(Icons.event_outlined),
                title: Text(_fecha == null ? 'Sin fecha objetivo' : 'Para ${Formato.mesLargo(Formato.periodo(_fecha!))}'),
                subtitle: const Text('Fecha estimada (opcional)'),
                trailing: _fecha == null
                    ? null
                    : IconButton(icon: const Icon(Icons.close), onPressed: () => setState(() => _fecha = null)),
                onTap: () async {
                  final ahora = DateTime.now();
                  final f = await showDatePicker(
                    context: context,
                    initialDate: ahora.add(const Duration(days: 180)),
                    firstDate: ahora,
                    lastDate: DateTime(ahora.year + 30),
                  );
                  if (f != null) setState(() => _fecha = f);
                },
              ),
              const SizedBox(height: 12),
              FilledButton(
                onPressed: () {
                  if (!_form.currentState!.validate()) return;
                  Navigator.pop(
                    context,
                    MetaAhorro(
                      id: '',
                      nombre: _nombre.text.trim(),
                      proposito: _proposito.text.trim(),
                      tipo: _tipo,
                      montoObjetivo: parseMonto(_monto.text) ?? 0,
                      fechaObjetivo: _fecha,
                    ),
                  );
                },
                child: const Text('Crear meta'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// ------------------------------------------------------------------ Deudas

class _VistaDeudas extends StatelessWidget {
  const _VistaDeudas();

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<DeudasCubit, AsyncState<ResumenDeudas>>(
      builder: (context, state) => AsyncView<ResumenDeudas>(
        state: state,
        onRetry: context.read<DeudasCubit>().cargar,
        onRefresh: context.read<DeudasCubit>().cargar,
        builder: (context, resumen) => ListView(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
          children: [
            if (resumen.deudas.isNotEmpty) _Alerta(resumen: resumen),
            if (resumen.deudas.isEmpty)
              const Tarjeta(child: Text('No tienes deudas registradas. ¡Que siga así! Si tienes alguna, regístrala para ver cuánto falta.')),
            for (final d in resumen.deudas) ...[_TarjetaDeuda(deuda: d), const SizedBox(height: 12)],
            OutlinedButton.icon(
              onPressed: () => _nuevaDeuda(context),
              icon: const Icon(Icons.add),
              label: const Text('Registrar una deuda'),
            ),
            const SizedBox(height: 20),
            Center(
              child: Text('No debáis a nadie nada, sino el amaros unos a otros.\nRomanos 13:8',
                  textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.terracota, fontStyle: FontStyle.italic)),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _nuevaDeuda(BuildContext context) async {
    final cubit = context.read<DeudasCubit>();
    final deuda = await showModalBottomSheet<NuevaDeuda>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (_) => const _FormDeuda(),
    );
    if (deuda == null || !context.mounted) return;
    final error = await cubit.crear(deuda);
    if (context.mounted) avisar(context, error ?? 'Deuda registrada', error: error != null);
  }
}

class _Alerta extends StatelessWidget {
  const _Alerta({required this.resumen});

  final ResumenDeudas resumen;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final sobre = resumen.superaLimite;
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Tarjeta(
        color: sobre ? AppColors.rojoTinte : AppColors.verdeTinte,
        child: Row(
          children: [
            Icon(sobre ? Icons.warning_amber_rounded : Icons.check_circle_outline, color: sobre ? AppColors.rojo : AppColors.verde),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                sobre
                    ? 'Tus cuotas (${Formato.moneda(resumen.cuotaTotalMensual)}) son el ${Formato.porcentaje(resumen.porcentajeCuotasSobreIngreso)} de tu ingreso, por encima del ${Formato.porcentaje(resumen.limitePorcentaje)} recomendado.'
                    : 'Tus cuotas (${Formato.moneda(resumen.cuotaTotalMensual)}) son el ${Formato.porcentaje(resumen.porcentajeCuotasSobreIngreso)} de tu ingreso: dentro de un rango saludable.',
                style: t.bodyMedium,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _TarjetaDeuda extends StatelessWidget {
  const _TarjetaDeuda({required this.deuda});

  final Deuda deuda;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Tarjeta(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(deuda.nombre, style: t.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
          if (deuda.acreedor != null) Text(deuda.acreedor!, style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
          const SizedBox(height: 12),
          Text(Formato.moneda(deuda.saldoActual), style: t.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
          Text('saldo pendiente', style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
          const SizedBox(height: 12),
          BarraProgreso(valor: deuda.porcentajePagado / 100, color: AppColors.pagoDeuda),
          const SizedBox(height: 4),
          Text('${Formato.porcentaje(deuda.porcentajePagado)} pagado',
              style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
          const SizedBox(height: 12),
          FilaDato('Cuota mensual (calculada)', Formato.moneda(deuda.cuotaMensual)),
          FilaDato('Tasa efectiva anual', Formato.porcentaje(deuda.tasaAnual)),
          FilaDato('Meses restantes', deuda.mesesRestantes?.toString() ?? 'La cuota no cubre los intereses'),
          const SizedBox(height: 8),
          Align(
            alignment: Alignment.centerRight,
            child: FilledButton.tonalIcon(
              onPressed: () async {
                final cubit = context.read<DeudasCubit>();
                await context.push<bool>(Rutas.nuevoMovimiento(TipoMovimiento.pagoDeuda, deudaId: deuda.id));
                cubit.cargar();
              },
              style: FilledButton.styleFrom(minimumSize: const Size(0, 44)),
              icon: const Icon(Icons.payments_outlined),
              label: const Text('Registrar pago'),
            ),
          ),
        ],
      ),
    );
  }
}

class _FormDeuda extends StatefulWidget {
  const _FormDeuda();

  @override
  State<_FormDeuda> createState() => _FormDeudaState();
}

class _FormDeudaState extends State<_FormDeuda> {
  final _form = GlobalKey<FormState>();
  final _nombre = TextEditingController();
  final _acreedor = TextEditingController();
  final _monto = TextEditingController();
  final _saldo = TextEditingController();
  final _tasa = TextEditingController(text: '0');
  final _plazo = TextEditingController();
  int? _dia;

  @override
  void dispose() {
    for (final c in [_nombre, _acreedor, _monto, _saldo, _tasa, _plazo]) {
      c.dispose();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.fromLTRB(20, 0, 20, 20 + MediaQuery.viewInsetsOf(context).bottom),
      child: SingleChildScrollView(
        child: Form(
          key: _form,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text('Registrar una deuda', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 16),
              TextFormField(
                controller: _nombre,
                autofocus: true,
                textCapitalization: TextCapitalization.sentences,
                decoration: const InputDecoration(labelText: 'Nombre (Tarjeta, préstamo…)'),
                validator: (v) => (v == null || v.trim().isEmpty) ? 'Ponle un nombre' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _acreedor,
                textCapitalization: TextCapitalization.sentences,
                decoration: const InputDecoration(labelText: 'A quién le debes (opcional)'),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _monto,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                inputFormatters: [formatoMonto],
                decoration: const InputDecoration(labelText: 'Monto original', prefixText: 'S/ '),
                validator: (v) {
                  final m = parseMonto(v ?? '');
                  return (m == null || m <= 0) ? 'Indica un monto mayor que cero' : null;
                },
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _saldo,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                inputFormatters: [formatoMonto],
                decoration: const InputDecoration(
                  labelText: 'Lo que debes hoy (opcional)',
                  prefixText: 'S/ ',
                  helperText: 'Si ya pagaste una parte',
                ),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: TextFormField(
                      controller: _tasa,
                      keyboardType: const TextInputType.numberWithOptions(decimal: true),
                      inputFormatters: [formatoMonto],
                      decoration: const InputDecoration(labelText: 'Tasa anual', suffixText: '%'),
                      validator: (v) => parseMonto(v ?? '') == null ? 'Inválida' : null,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: TextFormField(
                      controller: _plazo,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(labelText: 'Meses restantes'),
                      validator: (v) {
                        final n = int.tryParse(v ?? '');
                        return (n == null || n <= 0) ? 'Indica los meses' : null;
                      },
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<int?>(
                initialValue: _dia,
                decoration: const InputDecoration(labelText: 'Día de pago (opcional)'),
                items: [
                  const DropdownMenuItem<int?>(value: null, child: Text('Sin día fijo')),
                  for (var d = 1; d <= 31; d++) DropdownMenuItem<int?>(value: d, child: Text('Día $d')),
                ],
                onChanged: (v) => setState(() => _dia = v),
              ),
              const SizedBox(height: 8),
              Text('La cuota mensual se calcula sola a partir de lo que debes, la tasa y los meses.',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.textoSecundario)),
              const SizedBox(height: 16),
              FilledButton(
                onPressed: () {
                  if (!_form.currentState!.validate()) return;
                  final saldo = _saldo.text.trim().isEmpty ? null : parseMonto(_saldo.text);
                  Navigator.pop(
                    context,
                    NuevaDeuda(
                      nombre: _nombre.text.trim(),
                      acreedor: _acreedor.text.trim().isEmpty ? null : _acreedor.text.trim(),
                      montoOriginal: parseMonto(_monto.text) ?? 0,
                      saldoActual: saldo,
                      tasaAnual: parseMonto(_tasa.text) ?? 0,
                      plazoMeses: int.parse(_plazo.text),
                      diaPago: _dia,
                    ),
                  );
                },
                child: const Text('Registrar deuda'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
