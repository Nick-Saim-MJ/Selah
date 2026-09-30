package com.selahfinance.movimientos.application.port.out;

import java.util.UUID;

/**
 * Verifica que categoría, meta, deuda, etc. pertenezcan al mismo hogar del movimiento.
 * (La FK garantiza que existan; esto garantiza que no sean de otro hogar.)
 */
public interface ReferenciasHogarPort {

    boolean categoriaPertenece(UUID hogarId, UUID categoriaId);

    boolean fuenteIngresoPertenece(UUID hogarId, UUID fuenteIngresoId);

    boolean metaPertenece(UUID hogarId, UUID metaId);

    boolean deudaPertenece(UUID hogarId, UUID deudaId);

    boolean destinoOfrendaDisponible(UUID hogarId, UUID destinoOfrendaId);
}
