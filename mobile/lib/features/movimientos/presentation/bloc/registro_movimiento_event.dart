part of 'registro_movimiento_bloc.dart';

sealed class RegistroMovimientoEvent extends Equatable {
  const RegistroMovimientoEvent();

  @override
  List<Object?> get props => [];
}

/// Al abrir el formulario: carga categorías, metas o deudas según el tipo.
final class RegistroIniciado extends RegistroMovimientoEvent {
  const RegistroIniciado();
}

final class RegistroMontoCambiado extends RegistroMovimientoEvent {
  const RegistroMontoCambiado(this.monto);

  final double monto;

  @override
  List<Object?> get props => [monto];
}

/// Cambia solo los campos que se indican; los demás quedan como estaban.
final class RegistroDatosCambiados extends RegistroMovimientoEvent {
  const RegistroDatosCambiados({
    this.fecha,
    this.descripcion,
    this.categoriaId,
    this.metaId,
    this.deudaId,
    this.esImprevisto,
  });

  final DateTime? fecha;
  final String? descripcion;
  final String? categoriaId;
  final String? metaId;
  final String? deudaId;
  final bool? esImprevisto;

  @override
  List<Object?> get props => [fecha, descripcion, categoriaId, metaId, deudaId, esImprevisto];
}

final class RegistroEnviado extends RegistroMovimientoEvent {
  const RegistroEnviado();
}
