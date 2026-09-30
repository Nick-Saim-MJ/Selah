import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Guarda el JWT en el almacenamiento seguro del sistema (Keychain / Keystore),
/// nunca en SharedPreferences.
class TokenStorage {
  TokenStorage(this._storage);

  static const _clave = 'selah_access_token';

  final FlutterSecureStorage _storage;
  String? _cache;

  Future<String?> leer() async => _cache ??= await _storage.read(key: _clave);

  Future<void> guardar(String token) async {
    _cache = token;
    await _storage.write(key: _clave, value: token);
  }

  Future<void> borrar() async {
    _cache = null;
    await _storage.delete(key: _clave);
  }
}
