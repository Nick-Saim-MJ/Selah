import 'package:equatable/equatable.dart';

class Iglesia extends Equatable {
  const Iglesia({required this.id, required this.nombre, this.ciudad, this.distrito, this.activa = true});

  final String id;
  final String nombre;
  final String? ciudad;
  final String? distrito;
  final bool activa;

  /// "Adventista Central · Lima"
  String get etiqueta => ciudad == null ? nombre : '$nombre · $ciudad';

  @override
  List<Object?> get props => [id, nombre, ciudad, distrito, activa];
}
