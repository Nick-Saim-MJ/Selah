/// El onboarding se muestra solo en el primer uso (spec 4.1).
abstract interface class OnboardingRepository {
  bool fueVisto();

  Future<void> marcarVisto();
}
