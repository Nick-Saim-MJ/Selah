package com.selahfinance.deudas.domain.service;

import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.OptionalInt;

/**
 * La cuota de una deuda no se tipea: se calcula (sistema francés) desde saldo, TEA y plazo.
 * Rom. 13:8 · Prov. 22:7.
 */
public final class CalculadoraDeuda {

    /** Umbral de sobreendeudamiento del modelo SBS: cuotas &gt; 40% del ingreso. */
    public static final BigDecimal LIMITE_ENDEUDAMIENTO_PCT = BigDecimal.valueOf(40);

    /** Tasa efectiva mensual equivalente a una TEA expresada en % (p. ej. 24 = 24%). */
    public static double tasaMensual(BigDecimal teaPorcentaje) {
        return Math.pow(1 + teaPorcentaje.doubleValue() / 100.0, 1.0 / 12.0) - 1;
    }

    public Dinero cuotaMensual(Dinero saldo, BigDecimal teaPorcentaje, int plazoMeses) {
        if (plazoMeses <= 0) {
            throw new DomainException("PLAZO_INVALIDO", "El plazo debe ser mayor que cero");
        }
        double r = tasaMensual(teaPorcentaje);
        double s = saldo.valor().doubleValue();
        double cuota = r == 0 ? s / plazoMeses : s * r / (1 - Math.pow(1 + r, -plazoMeses));
        return Dinero.de(BigDecimal.valueOf(cuota).setScale(2, RoundingMode.HALF_UP));
    }

    /** Meses restantes con la cuota actual; vacío si la cuota no alcanza a cubrir los intereses. */
    public OptionalInt mesesRestantes(Dinero saldo, BigDecimal teaPorcentaje, Dinero cuota) {
        if (!saldo.esPositivo()) {
            return OptionalInt.of(0);
        }
        double r = tasaMensual(teaPorcentaje);
        double s = saldo.valor().doubleValue();
        double c = cuota.valor().doubleValue();
        if (r == 0) {
            return OptionalInt.of((int) Math.ceil(s / c));
        }
        if (c <= s * r) {
            return OptionalInt.empty();
        }
        return OptionalInt.of((int) Math.ceil(-Math.log(1 - r * s / c) / Math.log(1 + r)));
    }

    public boolean superaLimiteEndeudamiento(Dinero cuotasMensuales, Dinero ingresoMensual) {
        if (!ingresoMensual.esPositivo()) {
            return cuotasMensuales.esPositivo();
        }
        BigDecimal pct = cuotasMensuales.valor().multiply(BigDecimal.valueOf(100))
                .divide(ingresoMensual.valor(), 2, RoundingMode.HALF_EVEN);
        return pct.compareTo(LIMITE_ENDEUDAMIENTO_PCT) > 0;
    }
}
