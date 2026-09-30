import 'dart:convert';
import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:selah_finance/core/domain/dimension.dart';
import 'package:selah_finance/core/domain/rol_usuario.dart';
import 'package:selah_finance/core/domain/semaforo.dart';
import 'package:selah_finance/core/error/result.dart';
import 'package:selah_finance/features/admin/data/admin_repository_impl.dart';
import 'package:selah_finance/features/admin/domain/entities/admin_entities.dart';
import 'package:selah_finance/features/auth/data/datasources/auth_remote_data_source.dart';
import 'package:selah_finance/features/categorias/data/categorias_repository_impl.dart';
import 'package:selah_finance/features/dashboard/data/inicio_repository_impl.dart';
import 'package:selah_finance/features/habitos/data/habitos_repository_impl.dart';
import 'package:selah_finance/features/iglesias/data/iglesias_data.dart';
import 'package:selah_finance/features/mayordomia/data/repositories/mayordomia_repository_impl.dart';
import 'package:selah_finance/features/metas/data/metas_repository_impl.dart';
import 'package:selah_finance/features/notificaciones/data/notificaciones_repository_impl.dart';
import 'package:selah_finance/features/pastor/data/pastor_repository_impl.dart';
import 'package:selah_finance/features/reflexion/data/reflexion_repository_impl.dart';
import 'package:selah_finance/features/reportes/data/reportes_repository_impl.dart';

import '../support/fake_dio.dart';

/// Contrato app ↔ backend: los JSON de `test/fixtures` son respuestas REALES del backend (datos de demo).
/// Si el backend cambia un campo, estas pruebas fallan aquí y no en la presentación.
/// Para regenerarlos: levantar el backend con el perfil demo y correr scripts de captura (ver docs).
Object _fx(String nombre) => jsonDecode(File('test/fixtures/$nombre.json').readAsStringSync()) as Object;

T _ok<T>(Result<T> r) => switch (r) {
      Exito(:final valor) => valor,
      Fallo(:final failure) => throw TestFailure('Se esperaba éxito pero falló: ${failure.mensaje}'),
    };

void main() {
  test('Inicio: semáforo, saldo ya sin diezmo, diezmo pendiente y meta principal', () async {
    final fake = FakeDio({'GET /api/v1/inicio': _fx('inicio')});

    final i = _ok(await InicioRepositoryImpl(fake.dio).inicio());

    expect(i.semaforo, Semaforo.verde);
    expect(i.saldoDisponible, 830);
    expect(i.apartadoTotal, 540);
    expect(i.diezmoPendiente, 450);
    expect(i.categoriaDesvio, 'Alimentación');
    expect(i.metaPrincipal?.nombre, 'Fondo de emergencia');
    expect(i.metaPrincipal?.porcentaje, 65);
    expect(i.notificacionesNoLeidas, greaterThan(0));
  });

  test('Reporte financiero: 7 categorías con presupuestado vs real', () async {
    final fake = FakeDio({'GET /api/v1/reportes/financiero': _fx('reporte_financiero')});

    final r = _ok(await ReportesRepositoryImpl(fake.dio).financiero('2026-09'));

    expect(r.semaforo, Semaforo.verde);
    expect(r.categorias, hasLength(7));
    final alimentacion = r.categorias.firstWhere((c) => c.nombre == 'Alimentación');
    expect(alimentacion.real, 780);
    expect(alimentacion.planeado, 700);
    expect(alimentacion.variacionPct, 11.4);
    expect(alimentacion.excedida, isTrue);
    expect(r.principalDesvio?.nombre, 'Alimentación');
    expect(r.ratioTotal, closeTo(62.9, 0.1));
  });

  test('Reporte 4T: las cuatro dimensiones y el desglose de hábitos', () async {
    final fake = FakeDio({'GET /api/v1/reportes/mayordomia': _fx('reporte_mayordomia')});

    final r = _ok(await ReportesRepositoryImpl(fake.dio).mayordomia(null));

    for (final d in Dimension.values) {
      expect(r.puntajes[d], isNotNull, reason: d.etiqueta);
    }
    expect(r.global, isNotNull);
    expect(r.semaforo, isNotNull);
    expect(r.habitos[Dimension.templo], isNotEmpty);
    expect(r.habitos[Dimension.templo]!.first.cumplimientoPct, inInclusiveRange(0, 100));
  });

  test('Metas y deudas', () async {
    final fake = FakeDio({'GET /api/v1/metas': _fx('metas'), 'GET /api/v1/deudas': _fx('deudas')});
    final repo = MetasRepositoryImpl(fake.dio);

    final metas = _ok(await repo.metas());
    final emergencia = metas.firstWhere((m) => m.nombre == 'Fondo de emergencia');
    expect(emergencia.esPrincipal, isTrue);
    expect(emergencia.montoActual, 3900);
    expect(emergencia.montoObjetivo, 6000);
    expect(emergencia.aporteMensualSugerido, isNotNull);

    final deudas = _ok(await repo.deudas());
    expect(deudas.deudas, hasLength(1));
    expect(deudas.deudas.first.cuotaMensual, inInclusiveRange(320, 330));
    expect(deudas.deudas.first.mesesRestantes, isNotNull);
    expect(deudas.superaLimite, isFalse);
    expect(deudas.limitePorcentaje, 40);
  });

  test('Hábitos del día', () async {
    final fake = FakeDio({'GET /api/v1/habitos': _fx('habitos')});

    final h = _ok(await HabitosRepositoryImpl(fake.dio).delDia());

    expect(h.length, greaterThanOrEqualTo(17));
    expect(h.map((x) => x.dimension).toSet(), containsAll([Dimension.tiempo, Dimension.talento, Dimension.templo]));
    final agua = h.firstWhere((x) => x.codigo == 'AGUA');
    expect(agua.diaria, isTrue);
    expect(agua.metaValor, 8);
  });

  test('Reflexión semanal', () async {
    final fake = FakeDio({'GET /api/v1/reflexion/actual': _fx('reflexion')});

    final t = _ok(await ReflexionRepositoryImpl(fake.dio).actual());

    expect(t.pregunta.texto, isNotEmpty);
    expect(t.pregunta.referenciaBiblica, 'Lucas 12:15');
    expect(t.visible, isFalse); // capturado fuera de la ventana viernes 15:00 – sábado
  });

  test('Notificaciones y preferencias', () async {
    final fake = FakeDio({
      'GET /api/v1/notificaciones': _fx('notificaciones'),
      'GET /api/v1/notificaciones/preferencias': _fx('preferencias_notificacion'),
    });
    final repo = NotificacionesRepositoryImpl(fake.dio);

    final bandeja = _ok(await repo.bandeja());
    expect(bandeja.items.map((n) => n.tipo), containsAll(['PRESUPUESTO_EXCEDIDO', 'DIEZMO_PENDIENTE']));
    expect(bandeja.items.firstWhere((n) => n.tipo == 'DIEZMO_PENDIENTE').ruta, '/diezmos');

    final prefs = _ok(await repo.preferencias());
    expect(prefs.recordatorioDiezmo, isTrue);
  });

  test('Diezmos: historial de 6 meses, configuración y simulación', () async {
    final fake = FakeDio({
      'GET /api/v1/mayordomia/historial': _fx('diezmos_historial'),
      'GET /api/v1/hogar/configuracion-mayordomia': _fx('config_mayordomia'),
    });
    final repo = MayordomiaRepositoryImpl(fake.dio);

    final v = _ok(await repo.diezmos());
    expect(v.historial, hasLength(6));
    expect(v.mesActual.periodo, v.historial.last.periodo);
    expect(v.mesActual.diezmoPendiente, 450);
    expect(v.mesActual.diezmoAlDia, isFalse);
    expect(v.historial.first.diezmoAlDia, isTrue);

    final c = _ok(await repo.configuracion());
    expect(c.pctDiezmo, 10);
    expect(c.ofrendaActiva, isTrue);
    expect(c.diaEntregaDiezmo, 1);
    expect(c.modoSabadoActivo, isTrue);
  });

  test('Categorías y fuentes de ingreso', () async {
    final fake = FakeDio({'GET /api/v1/categorias': _fx('categorias'), 'GET /api/v1/fuentes-ingreso': _fx('fuentes_ingreso')});
    final repo = CategoriasRepositoryImpl(fake.dio);

    final cats = _ok(await repo.categorias());
    expect(cats, hasLength(7));
    expect(cats.firstWhere((c) => c.nombre == 'Vivienda').presupuestoMensual, 900);
    final fuentes = _ok(await repo.fuentes());
    expect(fuentes.single.montoEstimadoMensual, 4500);
  });

  test('Perfil (mi cuenta) e iglesias', () async {
    final fake = FakeDio({
      'GET /api/v1/perfil': _fx('perfil'),
      'GET /api/v1/iglesias': _fx('iglesias'),
      'GET /api/v1/admin/iglesias': _fx('admin_iglesias'),
    });

    final perfil = await AuthRemoteDataSourceImpl(fake.dio).perfil();
    expect(perfil.rol, RolUsuario.hermano);
    expect(perfil.iglesiaNombre, 'Adventista Central');
    expect(perfil.comparteReporte, isTrue);
    expect(perfil.nombreCompleto, 'Ana Torres');

    final repo = IglesiasRepositoryImpl(fake.dio);
    expect(_ok(await repo.activas()).map((i) => i.nombre), contains('Adventista Miraflores'));
    expect(_ok(await repo.todas()), hasLength(2));
  });

  test('Pastor: totales anónimos y reportes compartidos', () async {
    final fake = FakeDio({
      'GET /api/v1/pastor/congregacion': _fx('pastor_congregacion'),
      'GET /api/v1/pastor/reportes-compartidos': _fx('pastor_compartidos'),
    });

    final v = _ok(await PastorRepositoryImpl(fake.dio).congregacion('2026-09'));

    expect(v.resumen.miembrosActivos, 5);
    expect(v.resumen.datosSuficientes, isTrue);
    expect(v.resumen.minimoAnonimato, 3);
    expect(v.resumen.global, isNotNull);
    for (final d in Dimension.values) {
      expect(v.resumen.puntaje(d), isNotNull, reason: d.etiqueta);
    }
    expect(v.compartidos.map((c) => c.nombre), unorderedEquals(['Ana Torres', 'Luis Paredes', 'Marta Quispe']));
    final ana = v.compartidos.firstWhere((c) => c.nombre == 'Ana Torres');
    expect(ana.habitos, isNotEmpty);
    expect(ana.semaforo, isNotNull);
  });

  test('Admin: resumen, cuentas paginadas e integraciones', () async {
    final fake = FakeDio({
      'GET /api/v1/admin/usuarios/resumen': _fx('admin_resumen'),
      'GET /api/v1/admin/usuarios': _fx('admin_usuarios'),
      'GET /api/v1/admin/clientes-api': _fx('admin_clientes'),
      'GET /api/v1/admin/webhooks': _fx('admin_webhooks'),
      'POST /api/v1/admin/clientes-api': _fx('admin_cliente_creado'),
      'POST /api/v1/admin/webhooks': _fx('admin_webhook_creado'),
    });
    final repo = AdminRepositoryImpl(fake.dio);

    final resumen = _ok(await repo.resumen());
    expect(resumen.activosPorRol[RolUsuario.admin], 1);
    expect(resumen.activosPorRol[RolUsuario.pastor], 1);
    expect(resumen.activosPorRol[RolUsuario.hermano], 6);

    final pagina = _ok(await repo.usuarios(const FiltroUsuarios()));
    expect(pagina.total, 8);
    final ana = pagina.items.firstWhere((u) => u.email == 'ana@selah.demo');
    expect(ana.rol, RolUsuario.hermano);
    expect(ana.iglesiaNombre, 'Adventista Central');
    expect(ana.bloqueado, isFalse);
    expect(pagina.items.firstWhere((u) => u.rol == RolUsuario.admin).iglesiaId, isNull);

    final creado = _ok(await repo.crearCliente(const NuevoClienteApi(nombre: 'modulo-salud', scopes: ['habitos:write'])));
    expect(creado.apiKey, startsWith('sf_'));
    expect(creado.cliente.scopes, ['habitos:write']);

    final vista = _ok(await repo.integraciones());
    expect(vista.clientes.single.prefijoKey, startsWith('sf_'));
    expect(vista.webhooks.single.tipoEvento, '*');

    final webhook = _ok(await repo.crearWebhook(
        NuevoWebhook(clienteApiId: creado.cliente.id, tipoEvento: '*', url: 'https://equipo.ejemplo.com/hook')));
    expect(webhook.secreto, startsWith('whsec_'));
  });

  test('Un 422 del backend llega como mensaje legible', () async {
    final fake = FakeDio({'POST /api/v1/admin/clientes-api': 422});

    final r = await AdminRepositoryImpl(fake.dio).crearCliente(const NuevoClienteApi(nombre: 'x', scopes: ['habitos:write']));

    expect(r, isA<Fallo<ClienteCreado>>());
    expect((r as Fallo).failure.mensaje, 'error simulado');
  });
}
