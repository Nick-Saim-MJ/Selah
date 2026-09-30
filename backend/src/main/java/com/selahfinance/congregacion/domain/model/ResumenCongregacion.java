package com.selahfinance.congregacion.domain.model;

import com.selahfinance.shared.domain.Periodo;
import java.math.BigDecimal;

/**
 * Totales ANÓNIMOS de la congregación para el pastor: sin nombres, sin montos, sin datos individuales.
 * Si hay muy pocos miembros con datos, los promedios no se muestran ({@code datosSuficientes = false}):
 * con dos o tres personas un promedio delataría a cada una.
 *
 * @param fidelidadDiezmoPct % de hermanos con diezmo del mes que ya lo entregaron (null si no hay suficientes)
 */
public record ResumenCongregacion(
        Periodo periodo,
        int miembrosActivos,
        int miembrosConReporte,
        int minimoAnonimato,
        boolean datosSuficientes,
        BigDecimal fidelidadDiezmoPct,
        BigDecimal tiempo,
        BigDecimal talento,
        BigDecimal tesoro,
        BigDecimal templo,
        BigDecimal global) {
}
