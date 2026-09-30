import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:selah_finance/core/error/failures.dart';
import 'package:selah_finance/core/error/result.dart';
import 'package:selah_finance/features/movimientos/domain/entities/movimiento.dart';
import 'package:selah_finance/features/movimientos/domain/entities/tipo_movimiento.dart';
import 'package:selah_finance/features/movimientos/domain/repositories/movimientos_repository.dart';
import 'package:selah_finance/features/movimientos/domain/usecases/registrar_movimiento.dart';

class _RepoMock extends Mock implements MovimientosRepository {}

void main() {
  late _RepoMock repo;
  late RegistrarMovimiento registrar;
  final fecha = DateTime(2026, 9, 17);

  setUpAll(() => registerFallbackValue(NuevoMovimiento(tipo: TipoMovimiento.ingreso, monto: 1, fecha: fecha)));

  setUp(() {
    repo = _RepoMock();
    registrar = RegistrarMovimiento(repo);
  });

  test('un gasto sin categoría falla sin llamar al backend', () async {
    final r = await registrar(NuevoMovimiento(tipo: TipoMovimiento.gasto, monto: 180, fecha: fecha));

    expect(r, isA<Fallo<Movimiento>>());
    expect((r as Fallo).failure, isA<ValidacionFailure>());
    verifyNever(() => repo.registrar(any()));
  });

  test('el diezmo no se registra desde el formulario general', () async {
    final r = await registrar(NuevoMovimiento(tipo: TipoMovimiento.diezmo, monto: 450, fecha: fecha));

    expect(r, isA<Fallo<Movimiento>>());
  });

  test('un ingreso válido se delega al repositorio', () async {
    final nuevo = NuevoMovimiento(tipo: TipoMovimiento.ingreso, monto: 4500, fecha: fecha);
    final guardado = Movimiento(id: '1', tipo: TipoMovimiento.ingreso, monto: 4500, fecha: fecha);
    when(() => repo.registrar(nuevo)).thenAnswer((_) async => Exito(guardado));

    final r = await registrar(nuevo);

    expect(r, isA<Exito<Movimiento>>());
    expect((r as Exito<Movimiento>).valor, guardado);
  });
}
