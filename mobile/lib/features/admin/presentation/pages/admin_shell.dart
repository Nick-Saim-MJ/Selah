import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

/// Pestañas del administrador: Resumen | Usuarios | Iglesias | Integraciones. Sin finanzas.
class AdminShell extends StatelessWidget {
  const AdminShell({super.key, required this.shell});

  final StatefulNavigationShell shell;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: shell,
      bottomNavigationBar: NavigationBar(
        selectedIndex: shell.currentIndex,
        onDestinationSelected: (i) => shell.goBranch(i, initialLocation: i == shell.currentIndex),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.dashboard_outlined), selectedIcon: Icon(Icons.dashboard), label: 'Resumen'),
          NavigationDestination(icon: Icon(Icons.people_outline), selectedIcon: Icon(Icons.people), label: 'Usuarios'),
          NavigationDestination(icon: Icon(Icons.church_outlined), selectedIcon: Icon(Icons.church), label: 'Iglesias'),
          NavigationDestination(icon: Icon(Icons.hub_outlined), selectedIcon: Icon(Icons.hub), label: 'Integraciones'),
        ],
      ),
    );
  }
}
