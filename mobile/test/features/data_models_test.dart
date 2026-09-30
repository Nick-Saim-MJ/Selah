import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:selah_finance/core/domain/rol_usuario.dart';
import 'package:selah_finance/features/auth/data/models/sesion_model.dart';
import 'package:selah_finance/features/movimientos/data/models/movimiento_model.dart';
import 'package:selah_finance/features/movimientos/domain/entities/movimiento.dart';
import 'package:selah_finance/features/movimientos/domain/entities/tipo_movimiento.dart';

String _jwt(Map<String, dynamic> payload) {
  String b64(Object o) => base64Url.encode(utf8.encode(jsonEncode(o))).replaceAll('=', '');
  return '${b64({'alg': 'HS256'})}.${b64(payload)}.firma';
}

void main() {
  group('MovimientoModel', () {
    test('lee la respuesta del backend', () {
      final m = MovimientoModel.fromJson({
        'id': 'abc',
        'tipo': 'PAGO_DEUDA',
        'monto': 300.0,
        'fecha': '2026-09-10',
        'deudaId': 'd1',
        'esPresupuestado': true,
      });

      expect(m.tipo, TipoMovimiento.pagoDeuda);
      expect(m.montoConSigno, -300);
      expect(m.fecha, DateTime(2026, 9, 10));
    });

    test('envía categoría, meta y deuda solo cuando corresponden, y omite los nulos', () {
      final json = MovimientoModel.nuevoToJson(NuevoMovimiento(
        tipo: TipoMovimiento.gasto,
        monto: 12.345,
        fecha: DateTime(2026, 9, 17),
        categoriaId: 'c1',
        esPresupuestado: false,
      ));

      expect(json, {
        'tipo': 'GASTO',
        'monto': 12.35,
        'fecha': '2026-09-17',
        'categoriaId': 'c1',
        'esPresupuestado': false,
      });
    });
  });

  group('SesionModel.fromJson (respuesta de login)', () {
    test('hermano: con hogar e iglesia', () {
      final s = SesionModel.fromJson({
        'accessToken': 't',
        'usuarioId': 'u1',
        'hogarId': 'h1',
        'iglesiaId': 'i1',
        'rol': 'HERMANO',
        'nombres': 'Ana',
        'onboardingCompletado': true,
        'debeCambiarPassword': false,
      });

      expect(s.rol, RolUsuario.hermano);
      expect(s.hogarId, 'h1');
      expect(s.esAdmin, isFalse);
    });

    test('admin: el JSON no trae hogarId ni iglesiaId (los nulos no viajan)', () {
      final s = SesionModel.fromJson({
        'accessToken': 't',
        'usuarioId': 'u9',
        'rol': 'ADMIN',
        'nombres': 'Admin',
        'onboardingCompletado': false,
        'debeCambiarPassword': true,
      });

      expect(s.esAdmin, isTrue);
      expect(s.hogarId, isNull);
      expect(s.iglesiaId, isNull);
      expect(s.debeCambiarPassword, isTrue);
    });
  });

  group('SesionModel.desdeToken', () {
    final ahora = DateTime(2026, 9, 28, 12);
    int exp(Duration d) => ahora.add(d).millisecondsSinceEpoch ~/ 1000;

    test('restaura una sesión vigente con su rol', () {
      final token = _jwt({'sub': 'u1', 'hogar_id': 'h1', 'iglesia_id': 'i1', 'rol': 'PASTOR', 'exp': exp(const Duration(hours: 1))});

      final s = SesionModel.desdeToken(token, ahora: ahora);

      expect(s?.usuarioId, 'u1');
      expect(s?.hogarId, 'h1');
      expect(s?.iglesiaId, 'i1');
      expect(s?.rol, RolUsuario.pastor);
    });

    test('el token del admin no tiene hogar', () {
      final s = SesionModel.desdeToken(_jwt({'sub': 'u9', 'rol': 'ADMIN', 'exp': exp(const Duration(hours: 1))}), ahora: ahora);

      expect(s?.rol, RolUsuario.admin);
      expect(s?.hogarId, isNull);
    });

    test('descarta un token vencido, sin rol (versión anterior) o malformado', () {
      final vencido = _jwt({'sub': 'u1', 'hogar_id': 'h1', 'rol': 'HERMANO', 'exp': exp(const Duration(minutes: -1))});
      final sinRol = _jwt({'sub': 'u1', 'hogar_id': 'h1', 'exp': exp(const Duration(hours: 1))});

      expect(SesionModel.desdeToken(vencido, ahora: ahora), isNull);
      expect(SesionModel.desdeToken(sinRol, ahora: ahora), isNull);
      expect(SesionModel.desdeToken('no-es-un-jwt', ahora: ahora), isNull);
    });
  });
}
