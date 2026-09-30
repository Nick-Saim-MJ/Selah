import 'package:equatable/equatable.dart';

/// "De estos S/ 1000, S/ 100 son diezmo": se muestra ANTES de confirmar un ingreso.
class DesgloseIngreso extends Equatable {
  const DesgloseIngreso({
    required this.ingreso,
    required this.pctDiezmo,
    required this.diezmo,
    required this.pctOfrenda,
    required this.ofrenda,
    required this.disponible,
  });

  final double ingreso;
  final double pctDiezmo;
  final double diezmo;
  final double pctOfrenda;
  final double ofrenda;
  final double disponible;

  @override
  List<Object?> get props => [ingreso, pctDiezmo, diezmo, pctOfrenda, ofrenda, disponible];
}
