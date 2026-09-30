import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/bloc/datos_cambiados.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/colores.dart';
import '../../../../core/utils/entrada.dart';
import '../../../../core/utils/formato.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../../mayordomia/domain/entities/desglose_ingreso.dart';
import '../../domain/entities/tipo_movimiento.dart';
import '../bloc/registro_movimiento_bloc.dart';
import '../widgets/tipo_movimiento_ui.dart';

/// Formulario único de captura (Movimientos es la única fuente de datos de dinero).
class NuevoMovimientoPage extends StatefulWidget {
  const NuevoMovimientoPage({super.key});

  @override
  State<NuevoMovimientoPage> createState() => _NuevoMovimientoPageState();
}

class _NuevoMovimientoPageState extends State<NuevoMovimientoPage> {
  final _monto = TextEditingController();

  @override
  void dispose() {
    _monto.dispose();
    super.dispose();
  }

  void _ponerMonto(double valor) {
    _monto.text = montoParaCampo(valor);
    context.read<RegistroMovimientoBloc>().add(RegistroMontoCambiado(valor));
  }

  @override
  Widget build(BuildContext context) {
    final texto = Theme.of(context).textTheme;
    return BlocConsumer<RegistroMovimientoBloc, RegistroMovimientoState>(
      listenWhen: (a, b) => a.guardado != b.guardado || (b.error != null && a.error != b.error),
      listener: (context, state) {
        if (state.guardado != null) {
          avisar(context, 'Movimiento guardado');
          datosCambiados.avisar(context.read<RegistroMovimientoBloc>());
          context.pop(true);
        } else if (state.error != null) {
          avisar(context, state.error!, error: true);
        }
      },
      builder: (context, state) {
        final bloc = context.read<RegistroMovimientoBloc>();
        return Scaffold(
          appBar: AppBar(title: Text(state.tipo.tituloFormulario)),
          body: ListView(
            padding: const EdgeInsets.all(20),
            children: [
              Text('Monto', style: texto.labelLarge?.copyWith(color: AppColors.textoSecundario)),
              TextField(
                controller: _monto,
                autofocus: true,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                inputFormatters: [formatoMonto],
                style: texto.displaySmall?.copyWith(fontWeight: FontWeight.w800),
                decoration: const InputDecoration(prefixText: 'S/ ', hintText: '0.00'),
                onChanged: (v) => bloc.add(RegistroMontoCambiado(parseMonto(v) ?? 0)),
              ),
              const SizedBox(height: 16),
              if (state.desglose != null) _DesgloseCard(desglose: state.desglose!),
              if (state.cargandoOpciones) const Padding(padding: EdgeInsets.all(16), child: Center(child: CircularProgressIndicator())),
              if (state.tipo == TipoMovimiento.gasto && !state.cargandoOpciones) _SeccionGasto(state: state),
              if (state.tipo == TipoMovimiento.aporteMeta && !state.cargandoOpciones) _SeccionAporte(state: state),
              if (state.tipo == TipoMovimiento.pagoDeuda && !state.cargandoOpciones) _SeccionPago(state: state, alUsarCuota: _ponerMonto),
              const SizedBox(height: 16),
              TextField(
                decoration: InputDecoration(
                  labelText: state.tipo == TipoMovimiento.ofrenda ? 'Proyecto o destino (opcional)' : 'Descripción (opcional)',
                ),
                maxLength: 160,
                textCapitalization: TextCapitalization.sentences,
                onChanged: (v) => bloc.add(RegistroDatosCambiados(descripcion: v)),
              ),
              ListTile(
                contentPadding: EdgeInsets.zero,
                leading: const Icon(Icons.calendar_today_outlined),
                title: Text(Formato.fechaCorta(state.fecha)),
                subtitle: const Text('Fecha'),
                onTap: () async {
                  final fecha = await showDatePicker(
                    context: context,
                    initialDate: state.fecha,
                    firstDate: DateTime(2020),
                    lastDate: DateTime.now().add(const Duration(days: 1)),
                  );
                  if (fecha != null) bloc.add(RegistroDatosCambiados(fecha: fecha));
                },
              ),
            ],
          ),
          bottomNavigationBar: SafeArea(
            minimum: const EdgeInsets.all(20),
            child: FilledButton(
              onPressed: state.puedeEnviar ? () => bloc.add(const RegistroEnviado()) : null,
              child: state.enviando
                  ? const SizedBox.square(dimension: 22, child: CircularProgressIndicator(strokeWidth: 2.5))
                  : const Text('Guardar'),
            ),
          ),
        );
      },
    );
  }
}

/// El diezmo se muestra antes de confirmar, no después (spec 4.4 y 4.5).
class _DesgloseCard extends StatelessWidget {
  const _DesgloseCard({required this.desglose});

  final DesgloseIngreso desglose;

  @override
  Widget build(BuildContext context) {
    final texto = Theme.of(context).textTheme;
    return Tarjeta(
      color: AppColors.terracotaTinte,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Primero lo de Dios', style: texto.labelLarge?.copyWith(color: AppColors.terracotaOscuro)),
          const SizedBox(height: 8),
          FilaDato('Diezmo (${desglose.pctDiezmo.toStringAsFixed(0)}%)', Formato.moneda(desglose.diezmo)),
          if (desglose.ofrenda > 0) FilaDato('Ofrenda (${desglose.pctOfrenda.toStringAsFixed(0)}%)', Formato.moneda(desglose.ofrenda)),
          const Divider(),
          FilaDato('Disponible', Formato.moneda(desglose.disponible), destacado: true),
          const SizedBox(height: 4),
          Text('Proverbios 3:9', style: texto.labelSmall?.copyWith(color: AppColors.terracota)),
        ],
      ),
    );
  }
}

class _SeccionGasto extends StatelessWidget {
  const _SeccionGasto({required this.state});

  final RegistroMovimientoState state;

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RegistroMovimientoBloc>();
    final linea = state.presupuestoElegido;
    final restante = linea == null ? null : linea.restante - state.monto;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Categoría', style: Theme.of(context).textTheme.labelLarge?.copyWith(color: AppColors.textoSecundario)),
        const SizedBox(height: 8),
        if (state.categorias.isEmpty)
          const Tarjeta(child: Text('Aún no tienes categorías de gasto. Créalas en Configuración → Categorías y presupuesto.'))
        else
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              for (final c in state.categorias)
                ChoiceChip(
                  avatar: CircleAvatar(radius: 6, backgroundColor: colorDeHex(c.color)),
                  label: Text(c.nombre),
                  selected: state.categoriaId == c.id,
                  onSelected: (_) => bloc.add(RegistroDatosCambiados(categoriaId: c.id)),
                ),
            ],
          ),
        if (linea != null && linea.planeado > 0 && restante != null) ...[
          const SizedBox(height: 12),
          Tarjeta(
            color: restante >= 0 ? AppColors.verdeTinte : AppColors.rojoTinte,
            child: Text(
              restante >= 0
                  ? 'Te quedarán ${Formato.moneda(restante)} de ${linea.nombre} este mes.'
                  : 'Superarás el presupuesto de ${linea.nombre} por ${Formato.moneda(-restante)}.',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: restante >= 0 ? AppColors.verde : AppColors.rojo, fontWeight: FontWeight.w600),
            ),
          ),
        ],
        SwitchListTile(
          contentPadding: EdgeInsets.zero,
          title: const Text('Fue un gasto imprevisto'),
          subtitle: const Text('No lo tenías presupuestado'),
          value: state.esImprevisto,
          onChanged: (v) => bloc.add(RegistroDatosCambiados(esImprevisto: v)),
        ),
      ],
    );
  }
}

class _SeccionAporte extends StatelessWidget {
  const _SeccionAporte({required this.state});

  final RegistroMovimientoState state;

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RegistroMovimientoBloc>();
    final t = Theme.of(context).textTheme;
    final elegida = state.metas.where((m) => m.id == state.metaId).firstOrNull;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('¿A qué meta?', style: t.labelLarge?.copyWith(color: AppColors.textoSecundario)),
        const SizedBox(height: 8),
        if (state.metas.isEmpty)
          const Tarjeta(child: Text('No tienes metas activas. Crea una en la pestaña Metas.'))
        else
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              for (final m in state.metas)
                ChoiceChip(
                  label: Text('${m.nombre} · ${Formato.porcentaje(m.porcentaje)}'),
                  selected: state.metaId == m.id,
                  onSelected: (_) => bloc.add(RegistroDatosCambiados(metaId: m.id)),
                ),
            ],
          ),
        if (elegida != null) ...[
          const SizedBox(height: 12),
          Tarjeta(
            child: Column(
              children: [
                FilaDato('Llevas', Formato.moneda(elegida.montoActual)),
                FilaDato('Te faltan', Formato.moneda(elegida.restante)),
                if (elegida.aporteMensualSugerido != null) FilaDato('Sugerido al mes', Formato.moneda(elegida.aporteMensualSugerido!)),
              ],
            ),
          ),
        ],
      ],
    );
  }
}

class _SeccionPago extends StatelessWidget {
  const _SeccionPago({required this.state, required this.alUsarCuota});

  final RegistroMovimientoState state;
  final ValueChanged<double> alUsarCuota;

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RegistroMovimientoBloc>();
    final t = Theme.of(context).textTheme;
    final elegida = state.deudas.where((d) => d.id == state.deudaId).firstOrNull;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('¿Qué deuda pagas?', style: t.labelLarge?.copyWith(color: AppColors.textoSecundario)),
        const SizedBox(height: 8),
        if (state.deudas.isEmpty)
          const Tarjeta(child: Text('No tienes deudas activas registradas. Regístrala en Metas → Deudas.'))
        else
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              for (final d in state.deudas)
                ChoiceChip(
                  label: Text('${d.nombre} · ${Formato.moneda(d.saldoActual)}'),
                  selected: state.deudaId == d.id,
                  onSelected: (_) => bloc.add(RegistroDatosCambiados(deudaId: d.id)),
                ),
            ],
          ),
        if (elegida != null) ...[
          const SizedBox(height: 12),
          Tarjeta(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                FilaDato('Saldo pendiente', Formato.moneda(elegida.saldoActual)),
                FilaDato('Cuota mensual', Formato.moneda(elegida.cuotaMensual)),
                const SizedBox(height: 8),
                OutlinedButton(
                  onPressed: () => alUsarCuota(elegida.cuotaMensual),
                  style: OutlinedButton.styleFrom(minimumSize: const Size(0, 44)),
                  child: Text('Pagar la cuota (${Formato.moneda(elegida.cuotaMensual)})'),
                ),
                const SizedBox(height: 6),
                Text('Primero se cubren los intereses del mes y el resto baja tu saldo.', style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
              ],
            ),
          ),
        ],
      ],
    );
  }
}
