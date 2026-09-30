package com.selahfinance.categorias.application.port.out;

import com.selahfinance.categorias.domain.model.FuenteIngreso;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuenteIngresoRepositoryPort {

    FuenteIngreso guardar(FuenteIngreso fuente);

    Optional<FuenteIngreso> porId(UUID hogarId, UUID fuenteId);

    List<FuenteIngreso> activasDelHogar(UUID hogarId);

    boolean existeNombre(UUID hogarId, String nombre, UUID excluirId);
}
