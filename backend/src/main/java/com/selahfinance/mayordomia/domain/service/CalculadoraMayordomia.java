package com.selahfinance.mayordomia.domain.service;

import com.selahfinance.mayordomia.domain.model.ApartadoMayordomia;
import com.selahfinance.mayordomia.domain.model.DesgloseIngreso;
import com.selahfinance.mayordomia.domain.model.PoliticaMayordomia;
import com.selahfinance.mayordomia.domain.model.TipoApartado;
import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import com.selahfinance.shared.domain.Periodo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Regla central del producto (Mal. 3:10): el diezmo y la ofrenda se calculan sobre el
 * ingreso y se apartan PRIMERO, antes de mostrar lo disponible para gastar.
 */
public final class CalculadoraMayordomia {

    public DesgloseIngreso desglosar(Dinero ingreso, PoliticaMayordomia politica) {
        if (ingreso == null || !ingreso.esPositivo()) {
            throw new DomainException("MONTO_INVALIDO", "El ingreso debe ser mayor que cero");
        }
        Dinero diezmo = ingreso.porcentaje(politica.pctDiezmo());
        Dinero ofrenda = ingreso.porcentaje(politica.pctOfrenda());
        Dinero disponible = ingreso.restar(diezmo).restar(ofrenda);
        return new DesgloseIngreso(ingreso, politica.pctDiezmo(), diezmo, politica.pctOfrenda(), ofrenda, disponible);
    }

    /** Genera los apartados PENDIENTES de un ingreso (omite los de monto cero). */
    public List<ApartadoMayordomia> generarApartados(UUID hogarId, UUID movimientoIngresoId, Dinero ingreso,
            LocalDate fecha, PoliticaMayordomia politica) {
        var desglose = desglosar(ingreso, politica);
        var periodo = Periodo.de(fecha);
        var apartados = new ArrayList<ApartadoMayordomia>(2);
        if (desglose.diezmo().esPositivo()) {
            apartados.add(ApartadoMayordomia.pendiente(hogarId, movimientoIngresoId, TipoApartado.DIEZMO,
                    politica.pctDiezmo(), ingreso, desglose.diezmo(), periodo));
        }
        if (desglose.ofrenda().esPositivo()) {
            apartados.add(ApartadoMayordomia.pendiente(hogarId, movimientoIngresoId, TipoApartado.OFRENDA,
                    politica.pctOfrenda(), ingreso, desglose.ofrenda(), periodo));
        }
        return apartados;
    }
}
