package com.selahfinance.deudas.application.port.out;

import com.selahfinance.shared.domain.Dinero;
import java.time.YearMonth;
import java.util.UUID;

/** Base para medir el peso de las cuotas: ingreso estimado del hogar o, si no lo hay, el real del mes. */
public interface IngresoMensualPort {

    Dinero ingresoBase(UUID hogarId, YearMonth mes);
}
