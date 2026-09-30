package com.selahfinance.metas.application.port.in;

import com.selahfinance.metas.domain.model.MetaConProgreso;
import com.selahfinance.metas.domain.model.TipoMeta;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GestionarMetasUseCase {

    /** Metas no canceladas, con su progreso. La principal aparece primero. */
    List<MetaConProgreso> listar(UUID hogarId);

    MetaConProgreso obtener(UUID hogarId, UUID metaId);

    /** La primera meta del hogar se marca como principal automáticamente. */
    MetaConProgreso crear(UUID hogarId, Comando comando);

    MetaConProgreso actualizar(UUID hogarId, UUID metaId, Comando comando);

    MetaConProgreso marcarPrincipal(UUID hogarId, UUID metaId);

    void cancelar(UUID hogarId, UUID metaId);

    /** Meta que se destaca en Inicio. */
    Optional<MetaConProgreso> principal(UUID hogarId);

    record Comando(String nombre, String proposito, TipoMeta tipo, BigDecimal montoObjetivo, LocalDate fechaObjetivo) {
    }
}
