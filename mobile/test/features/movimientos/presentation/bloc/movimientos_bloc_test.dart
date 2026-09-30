import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:selah_finance/core/error/failures.dart';
import 'package:selah_finance/core/error/result.dart';
import 'package:selah_finance/features/movimientos/domain/entities/movimiento.dart';
import 'package:selah_finance/features/movimientos/domain/entities/tipo_movimiento.dart';
import 'package:selah_finance/features/movimientos/domain/usecases/eliminar_movimiento.dart';
import 'package:selah_finance/features/movimientos/domain/usecases/obtener_movimientos.dart';
import 'package:selah_finance/features/movimientos/presentation/bloc/movimientos_bloc.dart';

class _ObtenerMock extends Mock implements ObtenerMovimientos {}

class _EliminarMock extends Mock implements EliminarMovimiento {}

void main() {
  late _ObtenerMock obtener;
  final ingreso = Movimiento(id: '1', tipo: TipoMovimiento.ingreso, monto: 4500, fecha: DateTime(2026, 9, 17));

  setUpAll(() => registerFallbackValue(const FiltroMovimientos()));
  setUp(() => obtener = _ObtenerMock());

  blocTest<MovimientosBloc, MovimientosState>(
    'carga la primera página',
    setUp: () => when(() => obtener(any())).thenAnswer(
      (_) async => Exito(PaginaMovimientos(items: [ingreso], pagina: 0, totalPaginas: 1)),
    ),
    build: () => MovimientosBloc(obtener, _EliminarMock()),
    act: (b) => b.add(const MovimientosSolicitados()),
    expect: () => [
      const MovimientosState(estado: EstadoCarga.cargando),
      MovimientosState(estado: EstadoCarga.exito, items: [ingreso]),
    ],
  );

  blocTest<MovimientosBloc, MovimientosState>(
    'al filtrar pide solo ese tipo',
    setUp: () => when(() => obtener(any())).thenAnswer(
      (_) async => Exito(PaginaMovimientos(items: [ingreso], pagina: 0, totalPaginas: 1)),
    ),
    build: () => MovimientosBloc(obtener, _EliminarMock()),
    act: (b) => b.add(const MovimientosFiltroCambiado(TipoMovimiento.ingreso)),
    verify: (_) => verify(() => obtener(const FiltroMovimientos(tipo: TipoMovimiento.ingreso))).called(1),
  );

  blocTest<MovimientosBloc, MovimientosState>(
    'muestra el mensaje del fallo',
    setUp: () => when(() => obtener(any())).thenAnswer((_) async => const Fallo(ConexionFailure())),
    build: () => MovimientosBloc(obtener, _EliminarMock()),
    act: (b) => b.add(const MovimientosSolicitados()),
    expect: () => [
      const MovimientosState(estado: EstadoCarga.cargando),
      MovimientosState(estado: EstadoCarga.fallo, error: const ConexionFailure().mensaje),
    ],
  );
}
