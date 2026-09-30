package com.selahfinance.categorias.application.port.in;

import com.selahfinance.categorias.domain.model.FuenteIngreso;
import com.selahfinance.shared.domain.Dinero;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface GestionarFuentesIngresoUseCase {

    List<FuenteIngreso> listar(UUID hogarId);

    FuenteIngreso crear(UUID hogarId, String nombre, BigDecimal montoEstimadoMensual);

    FuenteIngreso actualizar(UUID hogarId, UUID fuenteId, String nombre, BigDecimal montoEstimadoMensual);

    void desactivar(UUID hogarId, UUID fuenteId);

    /** Suma de lo estimado en las fuentes activas (base del semáforo de deuda cuando aún no hay ingresos reales). */
    Dinero ingresoMensualEstimado(UUID hogarId);
}
