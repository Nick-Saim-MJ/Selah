import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/formato.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../../../core/widgets/secreto_dialog.dart';
import '../../domain/entities/admin_entities.dart';
import '../cubit/admin_cubits.dart';

const _scopesDisponibles = {
  'movimientos:read': 'Leer movimientos',
  'movimientos:write': 'Crear movimientos',
  'habitos:write': 'Registrar hábitos (Tiempo, Talento, Templo)',
  'reportes:read': 'Leer reportes',
};

const _eventos = {
  '*': 'Todos los eventos',
  'movimiento.registrado': 'Movimiento registrado',
  'movimiento.eliminado': 'Movimiento eliminado',
  'hogar.creado': 'Hogar creado',
};

/// Otros equipos se conectan aquí: API keys para que usen nuestra API y webhooks para avisarles de eventos.
class AdminIntegracionesPage extends StatelessWidget {
  const AdminIntegracionesPage({super.key});

  Future<void> _nuevoCliente(BuildContext context) async {
    final cubit = context.read<IntegracionesCubit>();
    final datos = await showModalBottomSheet<NuevoClienteApi>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (_) => const _FormCliente(),
    );
    if (datos == null || !context.mounted) return;
    final (error, creado) = await cubit.crearCliente(datos);
    if (!context.mounted) return;
    if (error != null || creado == null) {
      avisar(context, error ?? 'No se pudo crear', error: true);
      return;
    }
    await mostrarSecretoUnaVez(
      context,
      titulo: 'API key de ${creado.cliente.nombre}',
      secreto: creado.apiKey,
      aviso: 'Entrégala al equipo de ese sistema y pídeles que la envíen en el header X-API-Key. No se puede volver a ver: si la pierden, hay que crear otra.',
    );
  }

  Future<void> _nuevoWebhook(BuildContext context, List<ClienteApi> clientes) async {
    final cubit = context.read<IntegracionesCubit>();
    final datos = await showModalBottomSheet<NuevoWebhook>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (_) => _FormWebhook(clientes: clientes.where((c) => c.activo).toList()),
    );
    if (datos == null || !context.mounted) return;
    final (error, creado) = await cubit.crearWebhook(datos);
    if (!context.mounted) return;
    if (error != null || creado == null) {
      avisar(context, error ?? 'No se pudo crear', error: true);
      return;
    }
    await mostrarSecretoUnaVez(
      context,
      titulo: 'Secreto del webhook',
      secreto: creado.secreto,
      aviso: 'Con este secreto el otro equipo verifica la firma (header X-Selah-Firma, HMAC-SHA256 del cuerpo). No se puede volver a ver.',
    );
  }

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Integraciones')),
      body: BlocBuilder<IntegracionesCubit, AsyncState<VistaIntegraciones>>(
        builder: (context, state) => AsyncView<VistaIntegraciones>(
          state: state,
          onRetry: context.read<IntegracionesCubit>().cargar,
          onRefresh: context.read<IntegracionesCubit>().cargar,
          builder: (context, vista) {
            final nombres = {for (final c in vista.clientes) c.id: c.nombre};
            return ListView(
              padding: const EdgeInsets.fromLTRB(20, 0, 20, 32),
              children: [
                Text('Aquí das acceso a los sistemas de otros equipos y eliges de qué eventos avisarles.',
                    style: t.bodyMedium?.copyWith(color: AppColors.textoSecundario)),
                TituloSeccion('Sistemas conectados',
                    accion: TextButton.icon(onPressed: () => _nuevoCliente(context), icon: const Icon(Icons.add), label: const Text('Conectar'))),
                if (vista.clientes.isEmpty) const Tarjeta(child: Text('Ningún sistema conectado todavía.')),
                for (final c in vista.clientes) ...[_TarjetaCliente(cliente: c), const SizedBox(height: 8)],
                TituloSeccion('Webhooks',
                    accion: TextButton.icon(
                      onPressed: vista.clientes.any((c) => c.activo) ? () => _nuevoWebhook(context, vista.clientes) : null,
                      icon: const Icon(Icons.add),
                      label: const Text('Nuevo'),
                    )),
                if (vista.webhooks.isEmpty) const Tarjeta(child: Text('Sin webhooks. Se enviará un POST firmado a la URL que indiques cuando ocurra el evento.')),
                for (final w in vista.webhooks) ...[_TarjetaWebhook(webhook: w, cliente: nombres[w.clienteApiId] ?? '—'), const SizedBox(height: 8)],
              ],
            );
          },
        ),
      ),
    );
  }
}

class _TarjetaCliente extends StatelessWidget {
  const _TarjetaCliente({required this.cliente});

  final ClienteApi cliente;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    final c = cliente;
    return Tarjeta(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(child: Text(c.nombre, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700))),
              Etiqueta(c.activo ? 'Activo' : 'Revocado', color: c.activo ? AppColors.verde : AppColors.rojo),
            ],
          ),
          if (c.descripcion != null) Text(c.descripcion!, style: t.bodySmall?.copyWith(color: AppColors.textoSecundario)),
          const SizedBox(height: 8),
          Text('Key: ${c.prefijoKey}…', style: t.bodySmall?.copyWith(fontFamily: 'monospace')),
          const SizedBox(height: 6),
          Wrap(spacing: 6, runSpacing: 4, children: [for (final s in c.scopes) Etiqueta(s)]),
          const SizedBox(height: 6),
          Text(
            [
              if (c.responsableEmail != null) c.responsableEmail!,
              c.ultimoUso == null ? 'Sin uso todavía' : 'Último uso: ${Formato.fechaCorta(c.ultimoUso!.toLocal())}',
              if (c.expiraEn != null) 'Vence: ${Formato.fechaCorta(c.expiraEn!.toLocal())}',
            ].join(' · '),
            style: t.labelSmall?.copyWith(color: AppColors.textoTenue),
          ),
          if (c.activo)
            Align(
              alignment: Alignment.centerRight,
              child: TextButton.icon(
                style: TextButton.styleFrom(foregroundColor: AppColors.rojo),
                onPressed: () async {
                  final cubit = context.read<IntegracionesCubit>();
                  final si = await confirmar(context,
                      titulo: 'Revocar «${c.nombre}»',
                      mensaje: 'Su API key dejará de funcionar de inmediato. Esta acción no se puede deshacer.',
                      aceptar: 'Revocar');
                  if (!si) return;
                  final error = await cubit.revocar(c.id);
                  if (context.mounted) avisar(context, error ?? 'Acceso revocado', error: error != null);
                },
                icon: const Icon(Icons.block),
                label: const Text('Revocar'),
              ),
            ),
        ],
      ),
    );
  }
}

class _TarjetaWebhook extends StatelessWidget {
  const _TarjetaWebhook({required this.webhook, required this.cliente});

  final Webhook webhook;
  final String cliente;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Tarjeta(
      child: Row(
        children: [
          const Icon(Icons.webhook_outlined, color: AppColors.primario),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(_eventos[webhook.tipoEvento] ?? webhook.tipoEvento, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
                Text(webhook.url, style: t.bodySmall, maxLines: 2, overflow: TextOverflow.ellipsis),
                Text('Para: $cliente', style: t.labelSmall?.copyWith(color: AppColors.textoTenue)),
              ],
            ),
          ),
          IconButton(
            tooltip: 'Eliminar',
            icon: const Icon(Icons.delete_outline),
            onPressed: () async {
              final cubit = context.read<IntegracionesCubit>();
              if (!await confirmar(context, titulo: 'Eliminar webhook', mensaje: 'Dejarán de enviarse avisos a esa URL.', aceptar: 'Eliminar')) return;
              final error = await cubit.eliminarWebhook(webhook.id);
              if (context.mounted && error != null) avisar(context, error, error: true);
            },
          ),
        ],
      ),
    );
  }
}

class _FormCliente extends StatefulWidget {
  const _FormCliente();

  @override
  State<_FormCliente> createState() => _FormClienteState();
}

class _FormClienteState extends State<_FormCliente> {
  final _form = GlobalKey<FormState>();
  final _nombre = TextEditingController();
  final _descripcion = TextEditingController();
  final _email = TextEditingController();
  final _scopes = <String>{'habitos:write'};
  int? _dias;

  @override
  void dispose() {
    _nombre.dispose();
    _descripcion.dispose();
    _email.dispose();
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
              Text('Conectar un sistema', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 16),
              TextFormField(
                controller: _nombre,
                autofocus: true,
                decoration: const InputDecoration(labelText: 'Nombre del sistema (ej. Módulo de salud)'),
                validator: (v) => (v == null || v.trim().isEmpty) ? 'Ponle un nombre' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(controller: _descripcion, decoration: const InputDecoration(labelText: 'Descripción (opcional)')),
              const SizedBox(height: 12),
              TextFormField(
                controller: _email,
                keyboardType: TextInputType.emailAddress,
                decoration: const InputDecoration(labelText: 'Email del responsable (opcional)'),
                validator: (v) => (v == null || v.isEmpty || v.contains('@')) ? null : 'Email inválido',
              ),
              const SizedBox(height: 12),
              Text('Permisos', style: Theme.of(context).textTheme.labelLarge),
              for (final e in _scopesDisponibles.entries)
                CheckboxListTile(
                  contentPadding: EdgeInsets.zero,
                  dense: true,
                  title: Text(e.value),
                  subtitle: Text(e.key, style: const TextStyle(fontFamily: 'monospace', fontSize: 11)),
                  value: _scopes.contains(e.key),
                  onChanged: (v) => setState(() => v == true ? _scopes.add(e.key) : _scopes.remove(e.key)),
                ),
              DropdownButtonFormField<int?>(
                initialValue: _dias,
                decoration: const InputDecoration(labelText: 'Vigencia'),
                items: const [
                  DropdownMenuItem(value: null, child: Text('Sin vencimiento')),
                  DropdownMenuItem(value: 30, child: Text('30 días')),
                  DropdownMenuItem(value: 90, child: Text('90 días')),
                  DropdownMenuItem(value: 365, child: Text('1 año')),
                ],
                onChanged: (v) => setState(() => _dias = v),
              ),
              const SizedBox(height: 16),
              FilledButton(
                onPressed: _scopes.isEmpty
                    ? null
                    : () {
                        if (!_form.currentState!.validate()) return;
                        Navigator.pop(
                          context,
                          NuevoClienteApi(
                            nombre: _nombre.text.trim(),
                            descripcion: _descripcion.text.trim().isEmpty ? null : _descripcion.text.trim(),
                            responsableEmail: _email.text.trim().isEmpty ? null : _email.text.trim(),
                            scopes: _scopes.toList()..sort(),
                            diasVigencia: _dias,
                          ),
                        );
                      },
                child: const Text('Crear API key'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _FormWebhook extends StatefulWidget {
  const _FormWebhook({required this.clientes});

  final List<ClienteApi> clientes;

  @override
  State<_FormWebhook> createState() => _FormWebhookState();
}

class _FormWebhookState extends State<_FormWebhook> {
  final _form = GlobalKey<FormState>();
  final _url = TextEditingController();
  String? _clienteId;
  String _evento = 'movimiento.registrado';

  @override
  void dispose() {
    _url.dispose();
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
              Text('Nuevo webhook', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                initialValue: _clienteId,
                isExpanded: true,
                decoration: const InputDecoration(labelText: 'Sistema'),
                items: [for (final c in widget.clientes) DropdownMenuItem(value: c.id, child: Text(c.nombre))],
                onChanged: (v) => setState(() => _clienteId = v),
                validator: (v) => v == null ? 'Elige el sistema' : null,
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<String>(
                initialValue: _evento,
                isExpanded: true,
                decoration: const InputDecoration(labelText: 'Evento'),
                items: [for (final e in _eventos.entries) DropdownMenuItem(value: e.key, child: Text(e.value))],
                onChanged: (v) => setState(() => _evento = v ?? _evento),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _url,
                keyboardType: TextInputType.url,
                decoration: const InputDecoration(labelText: 'URL de destino', hintText: 'https://…'),
                validator: (v) {
                  final u = Uri.tryParse((v ?? '').trim());
                  return (u == null || !(u.isScheme('http') || u.isScheme('https')) || u.host.isEmpty) ? 'Debe empezar con http:// o https://' : null;
                },
              ),
              const SizedBox(height: 16),
              FilledButton(
                onPressed: () {
                  if (!_form.currentState!.validate()) return;
                  Navigator.pop(context, NuevoWebhook(clienteApiId: _clienteId!, tipoEvento: _evento, url: _url.text.trim()));
                },
                child: const Text('Crear webhook'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
