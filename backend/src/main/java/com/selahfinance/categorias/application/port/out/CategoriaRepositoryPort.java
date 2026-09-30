package com.selahfinance.categorias.application.port.out;

import com.selahfinance.categorias.domain.model.Categoria;
import com.selahfinance.categorias.domain.model.TipoCategoria;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepositoryPort {

    Categoria guardar(Categoria categoria);

    void guardarTodas(List<Categoria> categorias);

    Optional<Categoria> porId(UUID hogarId, UUID categoriaId);

    List<Categoria> activasDelHogar(UUID hogarId, TipoCategoria tipo);

    long contarDelHogar(UUID hogarId);

    boolean existeNombre(UUID hogarId, TipoCategoria tipo, String nombre, UUID excluirId);
}
