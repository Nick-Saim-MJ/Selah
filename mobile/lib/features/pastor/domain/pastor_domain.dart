import 'package:equatable/equatable.dart';

import '../../../core/domain/dimension.dart';
import '../../../core/domain/semaforo.dart';
import '../../../core/error/result.dart';
import '../../../core/usecase/usecase.dart';

/// Totales ANÓNIMOS de la congregación: sin nombres, sin montos. Con muy pocos miembros con datos
/// los promedios no se publican, para que un promedio no delate a una persona.
class ResumenCongregacion extends Equatable {
  const ResumenCongregacion({
    required this.periodo,
    required this.miembrosActivos,
    required this.miembrosConReporte,
    required this.minimoAnonimato,
    required this.datosSuficientes,
    this.fidelidadDiezmoPct,
    this.tiempo,
    this.talento,
    this.tesoro,
    this.templo,
    this.global,
  });

  final String periodo;
  final int miembrosActivos;
  final int miembrosConReporte;
  final int minimoAnonimato;
  final bool datosSuficientes;

  /// % de hermanos con diezmo del mes que ya lo entregaron.
  final double? fidelidadDiezmoPct;
  final double? tiempo;
  final double? talento;
  final double? tesoro;
  final double? templo;
  final double? global;

  double? puntaje(Dimension d) => switch (d) {
        Dimension.tiempo => tiempo,
        Dimension.talento => talento,
        Dimension.tesoro => tesoro,
        Dimension.templo => templo,
      };

  @override
  List<Object?> get props => [periodo, miembrosActivos, miembrosConReporte, minimoAnonimato, datosSuficientes,
        fidelidadDiezmoPct, tiempo, talento, tesoro, templo, global];
}

class HabitoCompartido extends Equatable {
  const HabitoCompartido({required this.nombre, required this.cumplimientoPct});

  final String nombre;
  final double cumplimientoPct;

  @override
  List<Object?> get props => [nombre, cumplimientoPct];
}

/// Reporte de un hermano que decidió compartirlo: nombre, puntajes, hábitos y diezmo sí/no. Nunca montos.
class ReporteCompartido extends Equatable {
  const ReporteCompartido({
    required this.usuarioId,
    required this.nombre,
    required this.puntajes,
    required this.habitos,
    this.global,
    this.semaforo,
    this.diezmoAlDia,
  });

  final String usuarioId;
  final String nombre;
  final Map<Dimension, double?> puntajes;
  final double? global;
  final Semaforo? semaforo;
  final bool? diezmoAlDia;
  final Map<Dimension, List<HabitoCompartido>> habitos;

  @override
  List<Object?> get props => [usuarioId, nombre, puntajes, global, semaforo, diezmoAlDia, habitos];
}

class VistaCongregacion extends Equatable {
  const VistaCongregacion({required this.resumen, required this.compartidos});

  final ResumenCongregacion resumen;
  final List<ReporteCompartido> compartidos;

  @override
  List<Object?> get props => [resumen, compartidos];
}

abstract interface class PastorRepository {
  /// `periodo` = YYYY-MM.
  Future<Result<VistaCongregacion>> congregacion(String periodo);
}

class ObtenerCongregacion implements UseCase<VistaCongregacion, String> {
  const ObtenerCongregacion(this._repo);

  final PastorRepository _repo;

  @override
  Future<Result<VistaCongregacion>> call(String periodo) => _repo.congregacion(periodo);
}
