package com.selahfinance.mayordomia.domain.model;

import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.Periodo;
import java.util.List;
import java.util.function.Predicate;

/** Estado del mes para la pantalla Diezmos y Ofrendas. */
public record ResumenMayordomia(
        Periodo periodo,
        Dinero diezmoApartado,
        Dinero diezmoPendiente,
        Dinero ofrendaApartada,
        Dinero ofrendaPendiente,
        boolean diezmoAlDia) {

    public static ResumenMayordomia de(Periodo periodo, List<ApartadoMayordomia> apartados) {
        Dinero diezmo = sumar(apartados, a -> a.tipo() == TipoApartado.DIEZMO && a.estado() != EstadoApartado.ANULADO);
        Dinero diezmoPend = sumar(apartados, a -> a.tipo() == TipoApartado.DIEZMO && a.estado() == EstadoApartado.PENDIENTE);
        Dinero ofrenda = sumar(apartados, a -> a.tipo() == TipoApartado.OFRENDA && a.estado() != EstadoApartado.ANULADO);
        Dinero ofrendaPend = sumar(apartados, a -> a.tipo() == TipoApartado.OFRENDA && a.estado() == EstadoApartado.PENDIENTE);
        return new ResumenMayordomia(periodo, diezmo, diezmoPend, ofrenda, ofrendaPend, !diezmoPend.esPositivo());
    }

    private static Dinero sumar(List<ApartadoMayordomia> apartados, Predicate<ApartadoMayordomia> filtro) {
        return apartados.stream().filter(filtro).map(ApartadoMayordomia::monto).reduce(Dinero.CERO, Dinero::sumar);
    }
}
