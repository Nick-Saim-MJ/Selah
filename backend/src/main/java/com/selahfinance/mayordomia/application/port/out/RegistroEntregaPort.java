package com.selahfinance.mayordomia.application.port.out;

import com.selahfinance.mayordomia.domain.model.TipoApartado;
import com.selahfinance.shared.domain.Dinero;
import java.time.LocalDate;
import java.util.UUID;

/** Registra la entrega como movimiento en la fuente única (módulo movimientos). */
public interface RegistroEntregaPort {

    UUID registrarEntrega(UUID hogarId, UUID usuarioId, TipoApartado tipo, Dinero monto, LocalDate fecha,
            String descripcion);
}
