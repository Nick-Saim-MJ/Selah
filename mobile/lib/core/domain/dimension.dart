/// Las cuatro dimensiones de la mayordomía cristiana adventista (mismo catálogo que el backend).
enum Dimension {
  tiempo('TIEMPO', 'Tiempo', 'Tu tiempo es un don de Dios: devoción, sábado, iglesia'),
  talento('TALENTO', 'Talento', 'Tus dones puestos al servicio de otros'),
  tesoro('TESORO', 'Tesoro', 'Tus recursos administrados con fidelidad'),
  templo('TEMPLO', 'Templo', 'Tu cuerpo, templo del Espíritu Santo');

  const Dimension(this.api, this.etiqueta, this.descripcion);

  final String api;
  final String etiqueta;
  final String descripcion;

  static Dimension desdeApi(String v) => values.firstWhere((d) => d.api == v);
}
