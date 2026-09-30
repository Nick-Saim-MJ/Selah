import 'package:flutter/widgets.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../bloc/async_state.dart';
import '../bloc/datos_cambiados.dart';

/// Ejecuta `alCambiar` cuando otra pantalla avisa que los datos cambiaron. Debe ir DENTRO del
/// BlocProvider de la pantalla, para poder leer su BLoC.
class RecargarAlCambiar extends StatefulWidget {
  const RecargarAlCambiar({super.key, required this.alCambiar, required this.child, this.propio});

  /// Recarga de un BLoC cualquiera. `origen`: el objeto que provocó el cambio.
  final void Function(BuildContext context) alCambiar;

  /// Devuelve el BLoC que provoca cambios desde esta pantalla, para no recargarlo dos veces.
  final Object? Function(BuildContext context)? propio;
  final Widget child;

  /// Atajo para pantallas basadas en un [AsyncCubit]: recarga `C` cuando cambian datos ajenos.
  static Widget cubit<C extends AsyncCubit<dynamic>>({Key? key, required Widget child}) => RecargarAlCambiar(
        key: key,
        alCambiar: (context) => context.read<C>().cargar(),
        propio: (context) => context.read<C>(),
        child: child,
      );

  @override
  State<RecargarAlCambiar> createState() => _RecargarAlCambiarState();
}

class _RecargarAlCambiarState extends State<RecargarAlCambiar> {
  @override
  void initState() {
    super.initState();
    datosCambiados.addListener(_alAvisar);
  }

  @override
  void dispose() {
    datosCambiados.removeListener(_alAvisar);
    super.dispose();
  }

  void _alAvisar() {
    if (!mounted) return;
    final propio = widget.propio?.call(context);
    if (propio != null && identical(propio, datosCambiados.origen)) return;
    widget.alCambiar(context);
  }

  @override
  Widget build(BuildContext context) => widget.child;
}
