import 'package:flutter/material.dart';

import '../widgets/categorias_editor.dart';
import '../widgets/fuentes_editor.dart';

/// Configuración → categorías de gasto y su presupuesto mensual.
class CategoriasPage extends StatelessWidget {
  const CategoriasPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Categorías y presupuesto')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: const [CategoriasEditor()],
      ),
    );
  }
}

/// Configuración → fuentes de ingreso.
class FuentesIngresoPage extends StatelessWidget {
  const FuentesIngresoPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Fuentes de ingreso')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: const [FuentesEditor()],
      ),
    );
  }
}
