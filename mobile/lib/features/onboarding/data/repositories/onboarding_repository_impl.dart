import 'package:shared_preferences/shared_preferences.dart';

import '../../domain/repositories/onboarding_repository.dart';

class OnboardingRepositoryImpl implements OnboardingRepository {
  const OnboardingRepositoryImpl(this._prefs);

  static const _clave = 'onboarding_visto';

  final SharedPreferences _prefs;

  @override
  bool fueVisto() => _prefs.getBool(_clave) ?? false;

  @override
  Future<void> marcarVisto() => _prefs.setBool(_clave, true);
}
