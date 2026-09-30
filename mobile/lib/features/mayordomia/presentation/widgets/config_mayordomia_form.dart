import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/entrada.dart';
import '../../domain/entities/mayordomia_entities.dart';

/// Formulario de porcentajes de diezmo/ofrenda, día de entrega y Modo Sábado.
/// Se usa en el asistente inicial y en Configuración. No guarda por sí mismo: avisa cada cambio con
/// `onCambio` para que quien lo contiene decida cuándo guardar.
class ConfigMayordomiaForm extends StatefulWidget {
  const ConfigMayordomiaForm({super.key, required this.inicial, required this.onCambio});

  final ConfigMayordomia inicial;
  final ValueChanged<ConfigMayordomia> onCambio;

  @override
  State<ConfigMayordomiaForm> createState() => _ConfigMayordomiaFormState();
}

class _ConfigMayordomiaFormState extends State<ConfigMayordomiaForm> {
  late ConfigMayordomia _c = widget.inicial;
  late final _diezmo = TextEditingController(text: montoParaCampo(_c.pctDiezmo));
  late final _ofrenda = TextEditingController(text: montoParaCampo(_c.pctOfrenda));

  @override
  void dispose() {
    _diezmo.dispose();
    _ofrenda.dispose();
    super.dispose();
  }

  void _emitir(ConfigMayordomia c) {
    setState(() => _c = c);
    widget.onCambio(c);
  }

  @override
  Widget build(BuildContext context) {
    final texto = Theme.of(context).textTheme;
    final total = _c.pctDiezmo + (_c.ofrendaActiva ? _c.pctOfrenda : 0);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TextField(
          controller: _diezmo,
          keyboardType: const TextInputType.numberWithOptions(decimal: true),
          inputFormatters: [formatoMonto],
          decoration: const InputDecoration(labelText: 'Diezmo', suffixText: '% del ingreso', helperText: 'Lo habitual es 10%'),
          onChanged: (v) => _emitir(ConfigMayordomia(
            pctDiezmo: parseMonto(v) ?? 0,
            ofrendaActiva: _c.ofrendaActiva,
            pctOfrenda: _c.pctOfrenda,
            diaEntregaDiezmo: _c.diaEntregaDiezmo,
            modoSabadoActivo: _c.modoSabadoActivo,
          )),
        ),
        const SizedBox(height: 8),
        SwitchListTile(
          contentPadding: EdgeInsets.zero,
          title: const Text('Apartar también una ofrenda'),
          value: _c.ofrendaActiva,
          onChanged: (v) => _emitir(ConfigMayordomia(
            pctDiezmo: _c.pctDiezmo,
            ofrendaActiva: v,
            pctOfrenda: _c.pctOfrenda,
            diaEntregaDiezmo: _c.diaEntregaDiezmo,
            modoSabadoActivo: _c.modoSabadoActivo,
          )),
        ),
        if (_c.ofrendaActiva)
          TextField(
            controller: _ofrenda,
            keyboardType: const TextInputType.numberWithOptions(decimal: true),
            inputFormatters: [formatoMonto],
            decoration: const InputDecoration(labelText: 'Ofrenda', suffixText: '% del ingreso'),
            onChanged: (v) => _emitir(ConfigMayordomia(
              pctDiezmo: _c.pctDiezmo,
              ofrendaActiva: true,
              pctOfrenda: parseMonto(v) ?? 0,
              diaEntregaDiezmo: _c.diaEntregaDiezmo,
              modoSabadoActivo: _c.modoSabadoActivo,
            )),
          ),
        const SizedBox(height: 4),
        Text(
          total > 100 ? 'Diezmo + ofrenda no puede pasar de 100%' : 'De cada ingreso apartas ${total.toStringAsFixed(total == total.roundToDouble() ? 0 : 1)}% antes de gastar.',
          style: texto.bodySmall?.copyWith(color: total > 100 ? AppColors.rojo : AppColors.textoSecundario),
        ),
        const SizedBox(height: 16),
        DropdownButtonFormField<int?>(
          initialValue: _c.diaEntregaDiezmo,
          decoration: const InputDecoration(labelText: 'Día del mes en que entrego el diezmo', helperText: 'Te recordaremos ese día'),
          items: [
            const DropdownMenuItem<int?>(value: null, child: Text('Sin día fijo')),
            for (var d = 1; d <= 28; d++) DropdownMenuItem<int?>(value: d, child: Text('Día $d')),
          ],
          onChanged: (v) => _emitir(ConfigMayordomia(
            pctDiezmo: _c.pctDiezmo,
            ofrendaActiva: _c.ofrendaActiva,
            pctOfrenda: _c.pctOfrenda,
            diaEntregaDiezmo: v,
            modoSabadoActivo: _c.modoSabadoActivo,
          )),
        ),
        const SizedBox(height: 8),
        SwitchListTile(
          contentPadding: EdgeInsets.zero,
          title: const Text('Modo Sábado'),
          subtitle: const Text('Del viernes al atardecer al sábado al atardecer no recibirás avisos de gasto ni de consumo.'),
          value: _c.modoSabadoActivo,
          onChanged: (v) => _emitir(ConfigMayordomia(
            pctDiezmo: _c.pctDiezmo,
            ofrendaActiva: _c.ofrendaActiva,
            pctOfrenda: _c.pctOfrenda,
            diaEntregaDiezmo: _c.diaEntregaDiezmo,
            modoSabadoActivo: v,
          )),
        ),
      ],
    );
  }
}
