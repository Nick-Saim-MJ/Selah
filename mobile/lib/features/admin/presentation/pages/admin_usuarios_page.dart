import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/bloc/async_state.dart';
import '../../../../core/domain/rol_usuario.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/widgets/async_view.dart';
import '../../../../core/widgets/componentes.dart';
import '../../../../core/widgets/secreto_dialog.dart';
import '../../../iglesias/domain/entities/iglesia.dart';
import '../../../iglesias/presentation/cubit/iglesias_cubit.dart';
import '../../domain/entities/admin_entities.dart';
import '../cubit/admin_cubits.dart';

/// Gestión de cuentas: crear pastores y hermanos, moverlos de iglesia, bloquear y reiniciar contraseñas.
class AdminUsuariosPage extends StatefulWidget {
  const AdminUsuariosPage({super.key});

  @override
  State<AdminUsuariosPage> createState() => _AdminUsuariosPageState();
}

class _AdminUsuariosPageState extends State<AdminUsuariosPage> {
  final _busqueda = TextEditingController();

  @override
  void dispose() {
    _busqueda.dispose();
    super.dispose();
  }

  List<Iglesia> _iglesias() => context.read<IglesiasCubit>().state.datos ?? const [];

  Future<void> _nuevo() async {
    final cubit = context.read<UsuariosCubit>();
    final nuevo = await showModalBottomSheet<NuevoUsuario>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (_) => _FormNuevoUsuario(iglesias: _iglesias().where((i) => i.activa).toList()),
    );
    if (nuevo == null || !mounted) return;
    final error = await cubit.crear(nuevo);
    if (!mounted) return;
    if (error != null) {
      avisar(context, error, error: true);
      return;
    }
    await mostrarSecretoUnaVez(
      context,
      titulo: 'Cuenta creada',
      secreto: nuevo.passwordTemporal,
      aviso: 'Entrega esta contraseña temporal a ${nuevo.nombres}. Tendrá que cambiarla al ingresar y no se puede volver a ver.',
    );
  }

  Future<void> _detalle(UsuarioAdmin u) async {
    final accion = await showModalBottomSheet<_Accion>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (_) => _DetalleUsuario(usuario: u),
    );
    if (accion == null || !mounted) return;
    switch (accion) {
      case _Accion.editar:
        await _editar(u);
      case _Accion.cambiarEstado:
        await _cambiarEstado(u);
      case _Accion.resetear:
        await _resetear(u);
    }
  }

  Future<void> _editar(UsuarioAdmin u) async {
    final cubit = context.read<UsuariosCubit>();
    final cambio = await showModalBottomSheet<CambioUsuario>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (_) => _FormEditarUsuario(usuario: u, iglesias: _iglesias().where((i) => i.activa).toList()),
    );
    if (cambio == null || !mounted) return;
    final error = await cubit.actualizar(cambio);
    if (mounted) avisar(context, error ?? 'Cuenta actualizada', error: error != null);
  }

  Future<void> _cambiarEstado(UsuarioAdmin u) async {
    final cubit = context.read<UsuariosCubit>();
    final bloquear = !u.bloqueado;
    final si = await confirmar(
      context,
      titulo: bloquear ? 'Bloquear a ${u.nombres}' : 'Reactivar a ${u.nombres}',
      mensaje: bloquear
          ? 'Su sesión dejará de funcionar de inmediato y no podrá volver a ingresar hasta que la reactives.'
          : 'Podrá volver a ingresar con su contraseña.',
      aceptar: bloquear ? 'Bloquear' : 'Reactivar',
    );
    if (!si || !mounted) return;
    final error = await cubit.cambiarEstado(u.id, bloquear: bloquear);
    if (mounted) avisar(context, error ?? (bloquear ? 'Cuenta bloqueada' : 'Cuenta reactivada'), error: error != null);
  }

  Future<void> _resetear(UsuarioAdmin u) async {
    final cubit = context.read<UsuariosCubit>();
    final si = await confirmar(
      context,
      titulo: 'Restablecer contraseña',
      mensaje: 'Se le asignará una contraseña temporal y tendrá que cambiarla al ingresar.',
      aceptar: 'Restablecer',
    );
    if (!si || !mounted) return;
    final temporal = generarPasswordTemporal();
    final error = await cubit.resetearPassword(u.id, temporal);
    if (!mounted) return;
    if (error != null) {
      avisar(context, error, error: true);
      return;
    }
    await mostrarSecretoUnaVez(
      context,
      titulo: 'Contraseña temporal',
      secreto: temporal,
      aviso: 'Entrégasela a ${u.nombres}. Tendrá que cambiarla al ingresar y no se puede volver a ver.',
    );
  }

  @override
  Widget build(BuildContext context) {
    final cubit = context.read<UsuariosCubit>();
    return Scaffold(
      appBar: AppBar(title: const Text('Usuarios')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _nuevo,
        icon: const Icon(Icons.person_add_alt_1),
        label: const Text('Nueva cuenta'),
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 8),
            child: TextField(
              controller: _busqueda,
              textInputAction: TextInputAction.search,
              decoration: InputDecoration(
                hintText: 'Buscar por nombre o email',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _busqueda.text.isEmpty
                    ? null
                    : IconButton(
                        icon: const Icon(Icons.close),
                        onPressed: () {
                          _busqueda.clear();
                          cubit.filtrar(cubit.filtro.copyWith(texto: ''));
                          setState(() {});
                        },
                      ),
              ),
              onChanged: (_) => setState(() {}),
              onSubmitted: (v) => cubit.filtrar(cubit.filtro.copyWith(texto: v)),
            ),
          ),
          BlocBuilder<UsuariosCubit, AsyncState<PaginaUsuarios>>(
            buildWhen: (a, b) => true,
            builder: (context, _) => SizedBox(
              height: 48,
              child: ListView(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 20),
                children: [
                  _FiltroChip('Todos', cubit.filtro.rol == null && !cubit.filtro.bloqueados,
                      () => cubit.filtrar(cubit.filtro.copyWith(limpiarRol: true, bloqueados: false))),
                  for (final r in RolUsuario.values)
                    _FiltroChip(r.plural, cubit.filtro.rol == r && !cubit.filtro.bloqueados,
                        () => cubit.filtrar(cubit.filtro.copyWith(rol: r, bloqueados: false))),
                  _FiltroChip('Bloqueados', cubit.filtro.bloqueados,
                      () => cubit.filtrar(cubit.filtro.copyWith(limpiarRol: true, bloqueados: true))),
                ],
              ),
            ),
          ),
          Expanded(
            child: BlocBuilder<UsuariosCubit, AsyncState<PaginaUsuarios>>(
              builder: (context, state) => AsyncView<PaginaUsuarios>(
                state: state,
                onRetry: cubit.cargar,
                onRefresh: cubit.cargar,
                builder: (context, pagina) {
                  if (pagina.items.isEmpty) {
                    return ListView(children: const [SizedBox(height: 100), MensajeCentrado(icono: Icons.search_off, texto: 'No hay cuentas con ese filtro.')]);
                  }
                  return ListView.builder(
                    padding: const EdgeInsets.only(bottom: 96),
                    itemCount: pagina.items.length + (pagina.hayMas ? 1 : 0),
                    itemBuilder: (context, i) {
                      if (i == pagina.items.length) {
                        return Padding(
                          padding: const EdgeInsets.all(12),
                          child: Center(child: TextButton(onPressed: cubit.verMas, child: Text('Ver más (${pagina.total - pagina.items.length} restantes)'))),
                        );
                      }
                      return _FilaUsuario(usuario: pagina.items[i], onTap: () => _detalle(pagina.items[i]));
                    },
                  );
                },
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _FiltroChip extends StatelessWidget {
  const _FiltroChip(this.texto, this.seleccionado, this.onTap);

  final String texto;
  final bool seleccionado;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: ChoiceChip(label: Text(texto), selected: seleccionado, onSelected: (_) => onTap()),
    );
  }
}

class _FilaUsuario extends StatelessWidget {
  const _FilaUsuario({required this.usuario, required this.onTap});

  final UsuarioAdmin usuario;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return ListTile(
      onTap: onTap,
      contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
      leading: CircleAvatar(
        backgroundColor: usuario.bloqueado ? AppColors.rojoTinte : AppColors.chip,
        child: Text(usuario.nombres.characters.first.toUpperCase(),
            style: TextStyle(fontWeight: FontWeight.w700, color: usuario.bloqueado ? AppColors.rojo : AppColors.primario)),
      ),
      title: Text(usuario.nombreCompleto, style: t.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
      subtitle: Text(
        [usuario.email, if (usuario.iglesiaNombre != null) usuario.iglesiaNombre!].join(' · '),
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
      trailing: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          Etiqueta(usuario.rol.etiqueta, color: usuario.rol == RolUsuario.admin ? AppColors.terracota : AppColors.primario),
          if (usuario.bloqueado) const Padding(padding: EdgeInsets.only(top: 4), child: Etiqueta('Bloqueada', color: AppColors.rojo)),
          if (!usuario.bloqueado && usuario.debeCambiarPassword)
            const Padding(padding: EdgeInsets.only(top: 4), child: Etiqueta('Clave temporal', color: AppColors.ambar)),
        ],
      ),
    );
  }
}

// ------------------------------------------------------------------ Detalle y acciones

enum _Accion { editar, cambiarEstado, resetear }

class _DetalleUsuario extends StatelessWidget {
  const _DetalleUsuario({required this.usuario});

  final UsuarioAdmin usuario;

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Padding(
      padding: EdgeInsets.fromLTRB(20, 0, 20, 20 + MediaQuery.viewInsetsOf(context).bottom),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(usuario.nombreCompleto, style: t.titleLarge?.copyWith(fontWeight: FontWeight.w800)),
          Text(usuario.email, style: t.bodyMedium?.copyWith(color: AppColors.textoSecundario)),
          const SizedBox(height: 8),
          Wrap(
            spacing: 6,
            children: [
              Etiqueta(usuario.rol.etiqueta),
              if (usuario.iglesiaNombre != null) Etiqueta(usuario.iglesiaNombre!, color: AppColors.terracota),
              if (usuario.bloqueado) const Etiqueta('Bloqueada', color: AppColors.rojo),
            ],
          ),
          const SizedBox(height: 16),
          FilledButton.tonalIcon(
            onPressed: () => Navigator.pop(context, _Accion.editar),
            icon: const Icon(Icons.edit_outlined),
            label: const Text('Editar datos, iglesia o rol'),
          ),
          const SizedBox(height: 8),
          FilledButton.tonalIcon(
            onPressed: () => Navigator.pop(context, _Accion.resetear),
            icon: const Icon(Icons.lock_reset),
            label: const Text('Restablecer contraseña'),
          ),
          const SizedBox(height: 8),
          FilledButton.tonalIcon(
            onPressed: () => Navigator.pop(context, _Accion.cambiarEstado),
            style: FilledButton.styleFrom(foregroundColor: usuario.bloqueado ? AppColors.verde : AppColors.rojo),
            icon: Icon(usuario.bloqueado ? Icons.lock_open : Icons.block),
            label: Text(usuario.bloqueado ? 'Reactivar cuenta' : 'Bloquear cuenta'),
          ),
        ],
      ),
    );
  }
}

class _FormNuevoUsuario extends StatefulWidget {
  const _FormNuevoUsuario({required this.iglesias});

  final List<Iglesia> iglesias;

  @override
  State<_FormNuevoUsuario> createState() => _FormNuevoUsuarioState();
}

class _FormNuevoUsuarioState extends State<_FormNuevoUsuario> {
  final _form = GlobalKey<FormState>();
  final _email = TextEditingController();
  final _nombres = TextEditingController();
  final _apellidos = TextEditingController();
  late final _password = TextEditingController(text: generarPasswordTemporal());
  RolUsuario _rol = RolUsuario.pastor;
  String? _iglesiaId;

  @override
  void dispose() {
    for (final c in [_email, _nombres, _apellidos, _password]) {
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
              Text('Nueva cuenta', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 16),
              SegmentedButton<RolUsuario>(
                segments: [for (final r in RolUsuario.values) ButtonSegment(value: r, label: Text(r.etiqueta))],
                selected: {_rol},
                onSelectionChanged: (s) => setState(() => _rol = s.first),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _nombres,
                textCapitalization: TextCapitalization.words,
                decoration: const InputDecoration(labelText: 'Nombre'),
                validator: (v) => (v == null || v.trim().isEmpty) ? 'Ingresa el nombre' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _apellidos,
                textCapitalization: TextCapitalization.words,
                decoration: const InputDecoration(labelText: 'Apellidos (opcional)'),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _email,
                keyboardType: TextInputType.emailAddress,
                decoration: const InputDecoration(labelText: 'Email'),
                validator: (v) => (v == null || !v.contains('@')) ? 'Email inválido' : null,
              ),
              if (_rol.tieneFinanzas) ...[
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  initialValue: _iglesiaId,
                  isExpanded: true,
                  decoration: const InputDecoration(labelText: 'Iglesia'),
                  items: [for (final i in widget.iglesias) DropdownMenuItem(value: i.id, child: Text(i.etiqueta, overflow: TextOverflow.ellipsis))],
                  onChanged: (v) => setState(() => _iglesiaId = v),
                  validator: (v) => v == null ? 'Elige la iglesia' : null,
                ),
              ],
              const SizedBox(height: 12),
              TextFormField(
                controller: _password,
                decoration: InputDecoration(
                  labelText: 'Contraseña temporal',
                  helperText: 'La cambiará al ingresar por primera vez',
                  suffixIcon: IconButton(
                    tooltip: 'Generar otra',
                    icon: const Icon(Icons.autorenew),
                    onPressed: () => setState(() => _password.text = generarPasswordTemporal()),
                  ),
                ),
                validator: (v) => (v == null || v.length < 8) ? 'Mínimo 8 caracteres' : null,
              ),
              const SizedBox(height: 16),
              FilledButton(
                onPressed: () {
                  if (!_form.currentState!.validate()) return;
                  Navigator.pop(
                    context,
                    NuevoUsuario(
                      email: _email.text.trim(),
                      nombres: _nombres.text.trim(),
                      apellidos: _apellidos.text.trim().isEmpty ? null : _apellidos.text.trim(),
                      rol: _rol,
                      iglesiaId: _rol.tieneFinanzas ? _iglesiaId : null,
                      passwordTemporal: _password.text,
                    ),
                  );
                },
                child: const Text('Crear cuenta'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _FormEditarUsuario extends StatefulWidget {
  const _FormEditarUsuario({required this.usuario, required this.iglesias});

  final UsuarioAdmin usuario;
  final List<Iglesia> iglesias;

  @override
  State<_FormEditarUsuario> createState() => _FormEditarUsuarioState();
}

class _FormEditarUsuarioState extends State<_FormEditarUsuario> {
  final _form = GlobalKey<FormState>();
  late final _nombres = TextEditingController(text: widget.usuario.nombres);
  late final _apellidos = TextEditingController(text: widget.usuario.apellidos ?? '');
  late RolUsuario _rol = widget.usuario.rol;
  late String? _iglesiaId = widget.usuario.iglesiaId;

  @override
  void dispose() {
    _nombres.dispose();
    _apellidos.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final esAdmin = widget.usuario.rol == RolUsuario.admin;
    // Si la iglesia actual ya no está activa, se conserva como opción para no perder el valor
    final ids = widget.iglesias.map((i) => i.id).toSet();
    return Padding(
      padding: EdgeInsets.fromLTRB(20, 0, 20, 20 + MediaQuery.viewInsetsOf(context).bottom),
      child: SingleChildScrollView(
        child: Form(
          key: _form,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text('Editar cuenta', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 16),
              TextFormField(
                controller: _nombres,
                textCapitalization: TextCapitalization.words,
                decoration: const InputDecoration(labelText: 'Nombre'),
                validator: (v) => (v == null || v.trim().isEmpty) ? 'Ingresa el nombre' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _apellidos,
                textCapitalization: TextCapitalization.words,
                decoration: const InputDecoration(labelText: 'Apellidos'),
              ),
              if (!esAdmin) ...[
                const SizedBox(height: 12),
                SegmentedButton<RolUsuario>(
                  segments: const [
                    ButtonSegment(value: RolUsuario.pastor, label: Text('Pastor')),
                    ButtonSegment(value: RolUsuario.hermano, label: Text('Hermano')),
                  ],
                  selected: {_rol},
                  onSelectionChanged: (s) => setState(() => _rol = s.first),
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  initialValue: ids.contains(_iglesiaId) ? _iglesiaId : null,
                  isExpanded: true,
                  decoration: InputDecoration(labelText: 'Iglesia', helperText: widget.usuario.iglesiaNombre == null ? null : 'Actual: ${widget.usuario.iglesiaNombre}'),
                  items: [for (final i in widget.iglesias) DropdownMenuItem(value: i.id, child: Text(i.etiqueta, overflow: TextOverflow.ellipsis))],
                  onChanged: (v) => setState(() => _iglesiaId = v),
                ),
              ],
              const SizedBox(height: 16),
              FilledButton(
                onPressed: () {
                  if (!_form.currentState!.validate()) return;
                  Navigator.pop(
                    context,
                    CambioUsuario(
                      id: widget.usuario.id,
                      nombres: _nombres.text.trim(),
                      apellidos: _apellidos.text.trim(),
                      rol: esAdmin || _rol == widget.usuario.rol ? null : _rol,
                      iglesiaId: esAdmin || _iglesiaId == widget.usuario.iglesiaId ? null : _iglesiaId,
                    ),
                  );
                },
                child: const Text('Guardar cambios'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
