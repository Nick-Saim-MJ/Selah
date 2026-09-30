package com.selahfinance.mayordomia.application.port.in;

import com.selahfinance.mayordomia.domain.model.DesgloseIngreso;
import java.math.BigDecimal;
import java.util.UUID;

/** Vista previa del diezmo/ofrenda antes de confirmar un ingreso (no guarda nada). */
public interface SimularDesgloseUseCase {

    DesgloseIngreso simular(UUID hogarId, BigDecimal montoIngreso);
}
