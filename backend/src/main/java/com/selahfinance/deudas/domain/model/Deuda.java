package com.selahfinance.deudas.domain.model;

import com.selahfinance.deudas.domain.service.CalculadoraDeuda;
import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * Deuda del hogar (Rom. 13:8: no vivir atado a lo que no se puede pagar).
 * La cuota mensual NO se digita: se calcula desde saldo, TEA y plazo (sistema francés).
 */
public record Deuda(
        UUID id,
        UUID hogarId,
        String nombre,
        String acreedor,
        Dinero montoOriginal,
        Dinero saldoActual,
        BigDecimal tasaAnual,
        int plazoMeses,
        Dinero cuotaMensual,
        LocalDate fechaInicio,
        Integer diaPago,
        EstadoDeuda estado) {

    private static final CalculadoraDeuda CALCULADORA = new CalculadoraDeuda();

    public Deuda {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("DEUDA_NOMBRE_REQUERIDO", "La deuda necesita un nombre");
        }
        if (montoOriginal == null || !montoOriginal.esPositivo()) {
            throw new DomainException("DEUDA_MONTO_INVALIDO", "El monto original debe ser mayor que cero");
        }
        if (saldoActual == null || saldoActual.esNegativo() || saldoActual.compareTo(montoOriginal) > 0) {
            throw new DomainException("DEUDA_SALDO_INVALIDO", "El saldo debe estar entre 0 y el monto original");
        }
        if (tasaAnual == null || tasaAnual.signum() < 0) {
            throw new DomainException("DEUDA_TASA_INVALIDA", "La tasa no puede ser negativa");
        }
        if (plazoMeses <= 0) {
            throw new DomainException("PLAZO_INVALIDO", "El plazo debe ser mayor que cero");
        }
        if (diaPago != null && (diaPago < 1 || diaPago > 31)) {
            throw new DomainException("DIA_INVALIDO", "El día de pago debe estar entre 1 y 31");
        }
        nombre = nombre.trim();
    }

    /** Crea una deuda calculando su cuota. {@code saldoActual} puede ser menor al original (deuda ya empezada). */
    public static Deuda nueva(UUID hogarId, String nombre, String acreedor, Dinero montoOriginal, Dinero saldoActual,
            BigDecimal tasaAnual, int plazoMeses, LocalDate fechaInicio, Integer diaPago) {
        Dinero saldo = saldoActual == null ? montoOriginal : saldoActual;
        Dinero cuota = CALCULADORA.cuotaMensual(saldo, tasaAnual == null ? BigDecimal.ZERO : tasaAnual, plazoMeses);
        return new Deuda(UUID.randomUUID(), hogarId, nombre, acreedor, montoOriginal, saldo,
                tasaAnual == null ? BigDecimal.ZERO : tasaAnual, plazoMeses, cuota, fechaInicio, diaPago,
                saldo.esPositivo() ? EstadoDeuda.ACTIVA : EstadoDeuda.PAGADA);
    }

    public Deuda editar(String nuevoNombre, String nuevoAcreedor, Integer nuevoDiaPago) {
        return new Deuda(id, hogarId, nuevoNombre, nuevoAcreedor, montoOriginal, saldoActual, tasaAnual, plazoMeses,
                cuotaMensual, fechaInicio, nuevoDiaPago, estado);
    }

    /** Interés que genera el saldo en un mes (TEA convertida a tasa mensual efectiva). */
    public Dinero interesDelMes() {
        return Dinero.de(BigDecimal.valueOf(saldoActual.valor().doubleValue() * CalculadoraDeuda.tasaMensual(tasaAnual))
                .setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Aplica un pago: primero cubre el interés del mes y el resto reduce el capital.
     * No se acepta pagar más que saldo + interés.
     */
    public ResultadoPago aplicarPago(Dinero pago) {
        if (estado != EstadoDeuda.ACTIVA) {
            throw new DomainException("DEUDA_NO_ACTIVA", "La deuda «" + nombre + "» ya no está activa");
        }
        if (pago == null || !pago.esPositivo()) {
            throw new DomainException("PAGO_INVALIDO", "El pago debe ser mayor que cero");
        }
        Dinero interesMes = interesDelMes();
        Dinero interesPagado = pago.compareTo(interesMes) < 0 ? pago : interesMes;
        Dinero capital = pago.restar(interesPagado);
        if (capital.compareTo(saldoActual) > 0) {
            throw new DomainException("PAGO_EXCEDE_DEUDA",
                    "El pago supera lo que debes (saldo " + saldoActual + " + interés " + interesMes + ")");
        }
        Dinero nuevoSaldo = saldoActual.restar(capital);
        var actualizada = new Deuda(id, hogarId, nombre, acreedor, montoOriginal, nuevoSaldo, tasaAnual, plazoMeses,
                cuotaMensual, fechaInicio, diaPago, nuevoSaldo.esPositivo() ? EstadoDeuda.ACTIVA : EstadoDeuda.PAGADA);
        return new ResultadoPago(actualizada, new DetallePago(interesPagado, capital));
    }

    /** Deshace un pago (p. ej. se eliminó el movimiento): el capital vuelve al saldo. */
    public Deuda revertirPago(DetallePago detalle) {
        Dinero saldo = saldoActual.sumar(detalle.capital());
        if (saldo.compareTo(montoOriginal) > 0) {
            saldo = montoOriginal;
        }
        return new Deuda(id, hogarId, nombre, acreedor, montoOriginal, saldo, tasaAnual, plazoMeses, cuotaMensual,
                fechaInicio, diaPago, EstadoDeuda.ACTIVA);
    }

    /** Meses que faltan con la cuota actual; vacío si la cuota no alcanza ni para los intereses. */
    public OptionalInt mesesRestantes() {
        return CALCULADORA.mesesRestantes(saldoActual, tasaAnual, cuotaMensual);
    }

    /** Porcentaje del monto original ya pagado (0-100). */
    public BigDecimal porcentajePagado() {
        Dinero pagado = montoOriginal.restar(saldoActual);
        return pagado.valor().multiply(BigDecimal.valueOf(100)).divide(montoOriginal.valor(), 2, RoundingMode.HALF_EVEN);
    }

    public record ResultadoPago(Deuda deuda, DetallePago detalle) {
    }
}
