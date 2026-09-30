package com.selahfinance.mayordomia.application.port.out;

import com.selahfinance.mayordomia.domain.model.ApartadoMayordomia;
import com.selahfinance.mayordomia.domain.model.TipoApartado;
import com.selahfinance.shared.domain.Periodo;
import java.util.List;
import java.util.UUID;

public interface ApartadoRepositoryPort {

    void guardarTodos(List<ApartadoMayordomia> apartados);

    List<ApartadoMayordomia> porHogarYPeriodo(UUID hogarId, Periodo periodo);

    List<ApartadoMayordomia> pendientes(UUID hogarId, Periodo periodo, TipoApartado tipo);

    List<ApartadoMayordomia> porMovimientoIngreso(UUID movimientoIngresoId);
}
