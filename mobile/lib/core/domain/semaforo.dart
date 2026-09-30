/// Resumen visual de salud: verde / amarillo / rojo.
enum Semaforo {
  verde('VERDE'),
  amarillo('AMARILLO'),
  rojo('ROJO');

  const Semaforo(this.api);

  final String api;

  static Semaforo? desdeApi(String? valor) {
    if (valor == null) return null;
    return values.firstWhere((s) => s.api == valor);
  }
}
