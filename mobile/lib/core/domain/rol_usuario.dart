/// Roles del sistema (mismo catálogo que el backend).
enum RolUsuario {
  admin('ADMIN', 'Administrador'),
  pastor('PASTOR', 'Pastor'),
  hermano('HERMANO', 'Hermano');

  const RolUsuario(this.api, this.etiqueta);

  final String api;
  final String etiqueta;

  /// Plural en español (Administradores, Pastores, Hermanos).
  String get plural => this == RolUsuario.hermano ? '${etiqueta}s' : '${etiqueta}es';

  /// ¿Tiene finanzas personales (hogar)? El admin no.
  bool get tieneFinanzas => this != RolUsuario.admin;

  static RolUsuario desdeApi(String valor) => values.firstWhere((r) => r.api == valor);
}
