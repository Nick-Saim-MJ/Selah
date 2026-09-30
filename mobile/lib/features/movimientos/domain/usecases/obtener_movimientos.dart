import 'package:equatable/equatable.dart';

import '../../../../core/error/result.dart';
import '../../../../core/usecase/usecase.dart';
import '../entities/movimiento.dart';
import '../entities/tipo_movimiento.dart';
import '../repositories/movimientos_repository.dart';

class ObtenerMovimientos implements UseCase<PaginaMovimientos, FiltroMovimientos> {
  const ObtenerMovimientos(this._repository);

  final MovimientosRepository _repository;

  @override
  Future<Result<PaginaMovimientos>> call(FiltroMovimientos p) =>
      _repository.obtener(tipo: p.tipo, pagina: p.pagina, tamanio: p.tamanio);
}

class FiltroMovimientos extends Equatable {
  const FiltroMovimientos({this.tipo, this.pagina = 0, this.tamanio = 20});

  final TipoMovimiento? tipo;
  final int pagina;
  final int tamanio;

  @override
  List<Object?> get props => [tipo, pagina, tamanio];
}
