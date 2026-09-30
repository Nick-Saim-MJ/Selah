package com.selahfinance.mayordomia.application.usecase;

import com.selahfinance.mayordomia.application.port.in.ConsultarResumenMayordomiaUseCase;
import com.selahfinance.mayordomia.application.port.in.GestionarApartadosUseCase;
import com.selahfinance.mayordomia.application.port.in.SimularDesgloseUseCase;
import com.selahfinance.mayordomia.application.port.out.ApartadoRepositoryPort;
import com.selahfinance.mayordomia.application.port.out.PoliticaMayordomiaPort;
import com.selahfinance.mayordomia.domain.model.ApartadoMayordomia;
import com.selahfinance.mayordomia.domain.model.DesgloseIngreso;
import com.selahfinance.mayordomia.domain.model.ResumenMayordomia;
import com.selahfinance.mayordomia.domain.service.CalculadoraMayordomia;
import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.Periodo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class MayordomiaService implements SimularDesgloseUseCase, GestionarApartadosUseCase, ConsultarResumenMayordomiaUseCase {

    private final CalculadoraMayordomia calculadora = new CalculadoraMayordomia();
    private final PoliticaMayordomiaPort politicas;
    private final ApartadoRepositoryPort apartados;

    @Override
    @Transactional(readOnly = true)
    public DesgloseIngreso simular(UUID hogarId, BigDecimal montoIngreso) {
        return calculadora.desglosar(Dinero.de(montoIngreso), politicas.de(hogarId));
    }

    @Override
    @Transactional
    public void generarParaIngreso(UUID hogarId, UUID movimientoIngresoId, BigDecimal monto, LocalDate fecha) {
        apartados.guardarTodos(calculadora.generarApartados(
                hogarId, movimientoIngresoId, Dinero.de(monto), fecha, politicas.de(hogarId)));
    }

    @Override
    @Transactional
    public void anularParaIngreso(UUID movimientoIngresoId) {
        var delIngreso = apartados.porMovimientoIngreso(movimientoIngresoId);
        var anulados = delIngreso.stream().filter(ApartadoMayordomia::anularSiPendiente).toList();
        apartados.guardarTodos(anulados);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenMayordomia resumen(UUID hogarId, Periodo periodo) {
        return ResumenMayordomia.de(periodo, apartados.porHogarYPeriodo(hogarId, periodo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumenMayordomia> historial(UUID hogarId, Periodo hasta, int meses) {
        int cantidad = Math.min(Math.max(meses, 1), 24);
        return IntStream.range(0, cantidad)
                .mapToObj(i -> new Periodo(hasta.mes().minusMonths(cantidad - 1L - i)))
                .map(p -> resumen(hogarId, p))
                .toList();
    }
}
