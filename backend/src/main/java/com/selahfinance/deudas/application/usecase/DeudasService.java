package com.selahfinance.deudas.application.usecase;

import com.selahfinance.deudas.application.port.in.AplicarPagoDeudaUseCase;
import com.selahfinance.deudas.application.port.in.GestionarDeudasUseCase;
import com.selahfinance.deudas.application.port.out.DeudaRepositoryPort;
import com.selahfinance.deudas.application.port.out.IngresoMensualPort;
import com.selahfinance.deudas.domain.model.Deuda;
import com.selahfinance.deudas.domain.model.ResumenDeudas;
import com.selahfinance.deudas.domain.service.CalculadoraDeuda;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import com.selahfinance.shared.domain.Dinero;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.YearMonth;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class DeudasService implements GestionarDeudasUseCase, AplicarPagoDeudaUseCase {

    private final CalculadoraDeuda calculadora = new CalculadoraDeuda();
    private final DeudaRepositoryPort deudas;
    private final IngresoMensualPort ingresos;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public ResumenDeudas resumen(UUID hogarId) {
        var activas = deudas.activasDelHogar(hogarId);
        Dinero cuotaTotal = activas.stream().map(Deuda::cuotaMensual).reduce(Dinero.CERO, Dinero::sumar);
        Dinero ingreso = ingresos.ingresoBase(hogarId, YearMonth.now(clock));
        BigDecimal pct = ingreso.esPositivo()
                ? cuotaTotal.valor().multiply(BigDecimal.valueOf(100)).divide(ingreso.valor(), 2, RoundingMode.HALF_EVEN)
                : BigDecimal.ZERO;
        return new ResumenDeudas(activas, cuotaTotal, ingreso, pct, calculadora.superaLimiteEndeudamiento(cuotaTotal, ingreso));
    }

    @Override
    @Transactional
    public Deuda crear(UUID hogarId, ComandoCrear c) {
        var nueva = Deuda.nueva(hogarId, c.nombre(), c.acreedor(), Dinero.de(c.montoOriginal()),
                c.saldoActual() == null ? null : Dinero.de(c.saldoActual()), c.tasaAnual(), c.plazoMeses(),
                c.fechaInicio() == null ? java.time.LocalDate.now(clock) : c.fechaInicio(), c.diaPago());
        return deudas.guardar(nueva);
    }

    @Override
    @Transactional
    public Deuda actualizar(UUID hogarId, UUID deudaId, String nombre, String acreedor, Integer diaPago) {
        var deuda = deudas.porId(hogarId, deudaId).orElseThrow(() -> new RecursoNoEncontradoException("Deuda", deudaId));
        return deudas.guardar(deuda.editar(nombre, acreedor, diaPago));
    }

    @Override
    @Transactional
    public void aplicar(UUID hogarId, UUID movimientoId, UUID deudaId, BigDecimal monto) {
        var deuda = deudas.porId(hogarId, deudaId).orElseThrow(() -> new RecursoNoEncontradoException("Deuda", deudaId));
        var resultado = deuda.aplicarPago(Dinero.de(monto));
        deudas.guardar(resultado.deuda());
        deudas.guardarPago(movimientoId, deudaId, resultado.detalle());
    }

    @Override
    @Transactional
    public void revertir(UUID movimientoId) {
        deudas.pagoDeMovimiento(movimientoId).filter(p -> !p.revertido()).ifPresent(pago ->
                deudas.porIdSinHogar(pago.deudaId()).ifPresent(deuda -> {
                    deudas.guardar(deuda.revertirPago(pago.detalle()));
                    deudas.marcarPagoRevertido(movimientoId, clock.instant());
                }));
    }
}
