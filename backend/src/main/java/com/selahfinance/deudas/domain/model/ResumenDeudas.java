package com.selahfinance.deudas.domain.model;

import com.selahfinance.shared.domain.Dinero;
import java.math.BigDecimal;
import java.util.List;

/** Deudas activas y la alerta de sobreendeudamiento (cuotas > 40% del ingreso, modelo SBS). */
public record ResumenDeudas(
        List<Deuda> deudas,
        Dinero cuotaTotalMensual,
        Dinero ingresoMensualBase,
        BigDecimal porcentajeCuotasSobreIngreso,
        boolean superaLimite) {
}
