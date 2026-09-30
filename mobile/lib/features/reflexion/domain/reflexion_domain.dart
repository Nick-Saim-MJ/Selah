import 'package:equatable/equatable.dart';

import '../../../core/domain/dimension.dart';
import '../../../core/error/result.dart';
import '../../../core/usecase/usecase.dart';

class PreguntaReflexion extends Equatable {
  const PreguntaReflexion({required this.id, required this.texto, this.referenciaBiblica, this.dimension});

  final int id;
  final String texto;
  final String? referenciaBiblica;
  final Dimension? dimension;

  @override
  List<Object?> get props => [id, texto, referenciaBiblica, dimension];
}

/// La tarjeta de reflexión de la semana: solo lectura + una pregunta opcional (spec 4.9).
/// No hay entrada de datos financieros aquí.
class TarjetaReflexion extends Equatable {
  const TarjetaReflexion({
    required this.visible,
    required this.pregunta,
    required this.entro,
    required this.gasto,
    this.respuesta,
    this.diezmoAlDia,
  });

  /// Toca mostrarla en Inicio (viernes por la tarde y sábado).
  final bool visible;
  final PreguntaReflexion pregunta;
  final String? respuesta;
  final double entro;
  final double gasto;

  /// Null si este mes no hubo diezmo que entregar.
  final bool? diezmoAlDia;

  @override
  List<Object?> get props => [visible, pregunta, respuesta, entro, gasto, diezmoAlDia];
}

abstract interface class ReflexionRepository {
  Future<Result<TarjetaReflexion>> actual();

  Future<Result<TarjetaReflexion>> responder(String respuesta);
}

class ObtenerReflexion implements UseCase<TarjetaReflexion, SinParametros> {
  const ObtenerReflexion(this._repo);

  final ReflexionRepository _repo;

  @override
  Future<Result<TarjetaReflexion>> call(SinParametros p) => _repo.actual();
}

class ResponderReflexion implements UseCase<TarjetaReflexion, String> {
  const ResponderReflexion(this._repo);

  final ReflexionRepository _repo;

  @override
  Future<Result<TarjetaReflexion>> call(String respuesta) => _repo.responder(respuesta);
}
