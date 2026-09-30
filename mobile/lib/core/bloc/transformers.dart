import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';

/// Espera [duracion] sin nuevos eventos antes de procesar el último
/// (p. ej. recalcular el diezmo mientras el usuario escribe el monto).
EventTransformer<E> debounce<E>(Duration duracion) {
  return (eventos, mapper) {
    final controller = StreamController<E>();
    Timer? timer;
    late final StreamSubscription<E> sub;
    controller.onListen = () {
      sub = eventos.listen(
        (e) {
          timer?.cancel();
          timer = Timer(duracion, () => controller.add(e));
        },
        onError: controller.addError,
        onDone: () {
          timer?.cancel();
          controller.close();
        },
      );
    };
    controller.onCancel = () {
      timer?.cancel();
      return sub.cancel();
    };
    return controller.stream.asyncExpand(mapper);
  };
}
