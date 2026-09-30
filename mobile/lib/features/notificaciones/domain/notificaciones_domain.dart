import 'package:equatable/equatable.dart';

import '../../../core/error/result.dart';
import '../../../core/usecase/usecase.dart';

class Notificacion extends Equatable {
  const Notificacion({
    required this.id,
    required this.tipo,
    required this.categoria,
    required this.titulo,
    required this.fecha,
    required this.leida,
    this.cuerpo,
    this.ruta,
  });

  final String id;

  /// PRESUPUESTO_EXCEDIDO, CUOTA_DEUDA, DIEZMO_PENDIENTE, REFLEXION_SABADO, RESUMEN_SEMANAL
  final String tipo;

  /// CONSUMO, MAYORDOMIA, RECORDATORIO, SISTEMA
  final String categoria;
  final String titulo;
  final String? cuerpo;

  /// Pantalla a la que lleva al tocarla (ej. /diezmos).
  final String? ruta;
  final DateTime fecha;
  final bool leida;

  Notificacion comoLeida() =>
      Notificacion(id: id, tipo: tipo, categoria: categoria, titulo: titulo, cuerpo: cuerpo, ruta: ruta, fecha: fecha, leida: true);

  @override
  List<Object?> get props => [id, tipo, categoria, titulo, cuerpo, ruta, fecha, leida];
}

class Bandeja extends Equatable {
  const Bandeja({required this.items, required this.noLeidas});

  final List<Notificacion> items;
  final int noLeidas;

  @override
  List<Object?> get props => [items, noLeidas];
}

/// Qué avisos quiere recibir el usuario (spec 4.8).
class PreferenciasNotificacion extends Equatable {
  const PreferenciasNotificacion({
    this.recordatorioDiezmo = true,
    this.recordatorioDeudas = true,
    this.resumenSemanal = true,
    this.reflexionSabado = true,
  });

  final bool recordatorioDiezmo;
  final bool recordatorioDeudas;
  final bool resumenSemanal;
  final bool reflexionSabado;

  PreferenciasNotificacion copyWith({bool? recordatorioDiezmo, bool? recordatorioDeudas, bool? resumenSemanal, bool? reflexionSabado}) =>
      PreferenciasNotificacion(
        recordatorioDiezmo: recordatorioDiezmo ?? this.recordatorioDiezmo,
        recordatorioDeudas: recordatorioDeudas ?? this.recordatorioDeudas,
        resumenSemanal: resumenSemanal ?? this.resumenSemanal,
        reflexionSabado: reflexionSabado ?? this.reflexionSabado,
      );

  @override
  List<Object?> get props => [recordatorioDiezmo, recordatorioDeudas, resumenSemanal, reflexionSabado];
}

abstract interface class NotificacionesRepository {
  Future<Result<Bandeja>> bandeja();

  Future<Result<void>> marcarLeida(String id);

  Future<Result<void>> marcarTodasLeidas();

  Future<Result<PreferenciasNotificacion>> preferencias();

  Future<Result<PreferenciasNotificacion>> guardarPreferencias(PreferenciasNotificacion p);
}

class ObtenerBandeja implements UseCase<Bandeja, SinParametros> {
  const ObtenerBandeja(this._repo);

  final NotificacionesRepository _repo;

  @override
  Future<Result<Bandeja>> call(SinParametros p) => _repo.bandeja();
}

class MarcarNotificacionLeida implements UseCase<void, String> {
  const MarcarNotificacionLeida(this._repo);

  final NotificacionesRepository _repo;

  @override
  Future<Result<void>> call(String id) => _repo.marcarLeida(id);
}

class MarcarTodasLeidas implements UseCase<void, SinParametros> {
  const MarcarTodasLeidas(this._repo);

  final NotificacionesRepository _repo;

  @override
  Future<Result<void>> call(SinParametros p) => _repo.marcarTodasLeidas();
}

class ObtenerPreferenciasNotificacion implements UseCase<PreferenciasNotificacion, SinParametros> {
  const ObtenerPreferenciasNotificacion(this._repo);

  final NotificacionesRepository _repo;

  @override
  Future<Result<PreferenciasNotificacion>> call(SinParametros p) => _repo.preferencias();
}

class GuardarPreferenciasNotificacion implements UseCase<PreferenciasNotificacion, PreferenciasNotificacion> {
  const GuardarPreferenciasNotificacion(this._repo);

  final NotificacionesRepository _repo;

  @override
  Future<Result<PreferenciasNotificacion>> call(PreferenciasNotificacion p) => _repo.guardarPreferencias(p);
}
