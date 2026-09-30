import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:go_router/go_router.dart';

import 'core/di/injection_container.dart';
import 'core/router/app_router.dart';
import 'core/theme/app_theme.dart';
import 'features/auth/presentation/bloc/auth_bloc.dart';
import 'features/onboarding/presentation/cubit/onboarding_cubit.dart';

class SelahFinanceApp extends StatefulWidget {
  const SelahFinanceApp({super.key});

  @override
  State<SelahFinanceApp> createState() => _SelahFinanceAppState();
}

class _SelahFinanceAppState extends State<SelahFinanceApp> {
  late final AuthBloc _auth = sl<AuthBloc>()..add(const AuthIniciado());
  late final OnboardingCubit _onboarding = sl<OnboardingCubit>();
  late final GoRouter _router = crearRouter(authBloc: _auth, onboardingCubit: _onboarding);

  @override
  void dispose() {
    _router.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider.value(value: _auth),
        BlocProvider.value(value: _onboarding),
      ],
      child: MaterialApp.router(
        title: 'SelahFinance',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.claro(),
        routerConfig: _router,
        locale: const Locale('es', 'PE'),
        supportedLocales: const [Locale('es', 'PE'), Locale('es')],
        localizationsDelegates: GlobalMaterialLocalizations.delegates,
      ),
    );
  }
}
