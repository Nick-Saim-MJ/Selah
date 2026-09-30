package com.selahfinance.categorias.infrastructure.persistence;

import com.selahfinance.categorias.application.port.out.CategoriaRepositoryPort;
import com.selahfinance.categorias.domain.model.Categoria;
import com.selahfinance.categorias.domain.model.TipoCategoria;
import com.selahfinance.shared.domain.Dinero;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CategoriaPersistenceAdapter implements CategoriaRepositoryPort {

    private final CategoriaJpaRepository categorias;

    @Override
    public Categoria guardar(Categoria c) {
        return aDominio(categorias.save(copiar(c, categorias.findById(c.id()).orElseGet(CategoriaJpaEntity::new))));
    }

    @Override
    public void guardarTodas(List<Categoria> lista) {
        categorias.saveAll(lista.stream().map(c -> copiar(c, new CategoriaJpaEntity())).toList());
    }

    @Override
    public Optional<Categoria> porId(UUID hogarId, UUID categoriaId) {
        return categorias.findByIdAndHogarId(categoriaId, hogarId).map(CategoriaPersistenceAdapter::aDominio);
    }

    @Override
    public List<Categoria> activasDelHogar(UUID hogarId, TipoCategoria tipo) {
        var filas = tipo == null
                ? categorias.findByHogarIdAndActivaTrueOrderByOrdenAscNombreAsc(hogarId)
                : categorias.findByHogarIdAndTipoAndActivaTrueOrderByOrdenAscNombreAsc(hogarId, tipo.name());
        return filas.stream().map(CategoriaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public long contarDelHogar(UUID hogarId) {
        return categorias.countByHogarId(hogarId);
    }

    @Override
    public boolean existeNombre(UUID hogarId, TipoCategoria tipo, String nombre, UUID excluirId) {
        return categorias.existeNombre(hogarId, tipo.name(), nombre, excluirId);
    }

    private static CategoriaJpaEntity copiar(Categoria c, CategoriaJpaEntity e) {
        e.setId(c.id());
        e.setHogarId(c.hogarId());
        e.setNombre(c.nombre());
        e.setTipo(c.tipo().name());
        e.setColor(c.color());
        e.setIcono(c.icono());
        e.setPresupuestoMensual(c.presupuestoMensual().valor());
        e.setOrden((short) c.orden());
        e.setActiva(c.activa());
        return e;
    }

    private static Categoria aDominio(CategoriaJpaEntity e) {
        return new Categoria(e.getId(), e.getHogarId(), e.getNombre(), TipoCategoria.valueOf(e.getTipo()), e.getColor(),
                e.getIcono(), Dinero.de(e.getPresupuestoMensual()), e.getOrden(), e.isActiva());
    }
}
