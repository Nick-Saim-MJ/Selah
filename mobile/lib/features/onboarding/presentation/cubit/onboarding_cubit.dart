import 'package:flutter_bloc/flutter_bloc.dart';

import '../../domain/repositories/onboarding_repository.dart';

/// Estado: `true` si el usuario ya vio el onboarding.
class OnboardingCubit extends Cubit<bool> {
  OnboardingCubit(this._repository) : super(_repository.fueVisto());

  final OnboardingRepository _repository;

  Future<void> completar() async {
    await _repository.marcarVisto();
    emit(true);
  }
}
