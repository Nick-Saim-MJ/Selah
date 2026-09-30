import 'package:flutter/foundation.dart';

/// Aviso interno de "los datos del usuario cambiaron" (se registró un gasto, se entregó el diezmo,
/// se creó una meta…). Las pantallas que muestran datos derivados (Inicio, Diezmos, Metas, Reportes)
/// se recargan al recibirlo; así ninguna pestaña muestra cifras viejas (spec: Movimientos alimenta todo).
class DatosCambiados extends ChangeNotifier {
  Object? _origen;

  /// Quién provocó el último cambio (ya se recargó a sí mismo y no necesita volver a hacerlo).
  Object? get origen => _origen;

  void avisar(Object origen) {
    _origen = origen;
    notifyListeners();
  }
}

/// Instancia única de la app.
final datosCambiados = DatosCambiados();
