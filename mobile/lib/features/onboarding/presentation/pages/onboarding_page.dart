import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../core/theme/app_colors.dart';
import '../cubit/onboarding_cubit.dart';

/// 3 pantallas que presentan la mayordomía como identidad antes de pedir datos (1 Cor. 4:2).
class OnboardingPage extends StatefulWidget {
  const OnboardingPage({super.key});

  @override
  State<OnboardingPage> createState() => _OnboardingPageState();
}

class _OnboardingPageState extends State<OnboardingPage> {
  static const _pasos = [
    (
      icono: Icons.volunteer_activism_outlined,
      titulo: 'Todo lo que tienes es un préstamo de Dios',
      cuerpo: 'La mayordomía empieza aquí: administras, no posees. Eso cambia cómo decides.',
      cita: '1 Corintios 4:2',
    ),
    (
      icono: Icons.checklist_rtl_outlined,
      titulo: 'Organiza con orden, no con culpa',
      cuerpo: 'Un sistema claro para tus ingresos y gastos, sin juicios ni complicaciones.',
      cita: 'Lucas 14:28',
    ),
    (
      icono: Icons.wb_twilight_outlined,
      titulo: 'El sábado también descansa tus finanzas',
      cuerpo: 'Un espacio en tu semana sin notificaciones de gasto, solo para reflexionar.',
      cita: 'Éxodo 20:8-11',
    ),
  ];

  final _controller = PageController();
  int _paso = 0;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _siguiente() {
    if (_paso < _pasos.length - 1) {
      _controller.nextPage(duration: const Duration(milliseconds: 300), curve: Curves.easeOut);
    } else {
      context.read<OnboardingCubit>().completar();
    }
  }

  @override
  Widget build(BuildContext context) {
    final texto = Theme.of(context).textTheme;
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(24, 8, 24, 28),
          child: Column(
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('SelahFinance',
                      style: texto.titleLarge?.copyWith(fontWeight: FontWeight.w800, color: AppColors.primario)),
                  TextButton(
                    onPressed: () => context.read<OnboardingCubit>().completar(),
                    child: const Text('Omitir'),
                  ),
                ],
              ),
              Expanded(
                child: PageView.builder(
                  controller: _controller,
                  itemCount: _pasos.length,
                  onPageChanged: (i) => setState(() => _paso = i),
                  itemBuilder: (context, i) {
                    final p = _pasos[i];
                    return Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        CircleAvatar(
                          radius: 72,
                          backgroundColor: AppColors.terracotaTinte,
                          child: Icon(p.icono, size: 64, color: AppColors.terracota),
                        ),
                        const SizedBox(height: 32),
                        Text(p.titulo,
                            textAlign: TextAlign.center,
                            style: texto.headlineSmall?.copyWith(fontWeight: FontWeight.w700)),
                        const SizedBox(height: 12),
                        Text(p.cuerpo,
                            textAlign: TextAlign.center,
                            style: texto.bodyLarge?.copyWith(color: AppColors.textoSecundario)),
                        const SizedBox(height: 12),
                        Text(p.cita, style: texto.labelMedium?.copyWith(color: AppColors.terracota)),
                      ],
                    );
                  },
                ),
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  for (var i = 0; i < _pasos.length; i++)
                    AnimatedContainer(
                      duration: const Duration(milliseconds: 250),
                      margin: const EdgeInsets.symmetric(horizontal: 3),
                      width: i == _paso ? 24 : 7,
                      height: 7,
                      decoration: BoxDecoration(
                        color: i == _paso ? AppColors.primario : AppColors.borde,
                        borderRadius: BorderRadius.circular(4),
                      ),
                    ),
                ],
              ),
              const SizedBox(height: 24),
              FilledButton(
                onPressed: _siguiente,
                child: Text(_paso < _pasos.length - 1 ? 'Siguiente' : 'Comenzar'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
