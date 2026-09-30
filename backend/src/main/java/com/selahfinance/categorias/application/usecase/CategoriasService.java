package com.selahfinance.categorias.application.usecase;

import com.selahfinance.categorias.application.port.in.GestionarCategoriasUseCase;
import com.selahfinance.categorias.application.port.out.CategoriaRepositoryPort;
import com.selahfinance.categorias.domain.model.Categoria;
import com.selahfinance.categorias.domain.model.TipoCategoria;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CategoriasService implements GestionarCategoriasUseCase {

    private final CategoriaRepositoryPort categorias;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listar(UUID hogarId, TipoCategoria tipo) {
        return categorias.activasDelHogar(hogarId, tipo);
    }

    @Override
    @Transactional
    public Categoria crear(UUID hogarId, Comando c) {
        var tipo = c.tipo() == null ? TipoCategoria.GASTO : c.tipo();
        var siguienteOrden = categorias.activasDelHogar(hogarId, tipo).size();
        var nueva = Categoria.nueva(hogarId, c.nombre(), tipo, c.color(), c.icono(), presupuesto(c), siguienteOrden);
        verificarNombreLibre(nueva, null);
        return categorias.guardar(nueva);
    }

    @Override
    @Transactional
    public Categoria actualizar(UUID hogarId, UUID categoriaId, Comando c) {
        var editada = cargar(hogarId, categoriaId).editar(c.nombre(), c.color(), c.icono(), presupuesto(c));
        verificarNombreLibre(editada, categoriaId);
        return categorias.guardar(editada);
    }

    @Override
    @Transactional
    public void desactivar(UUID hogarId, UUID categoriaId) {
        categorias.guardar(cargar(hogarId, categoriaId).conActiva(false));
    }

    @Override
    @Transactional
    public void sembrarPorDefecto(UUID hogarId) {
        if (categorias.contarDelHogar(hogarId) > 0) {
            return;
        }
        var sugeridas = Categoria.SUGERIDAS;
        categorias.guardarTodas(IntStream.range(0, sugeridas.size())
                .mapToObj(i -> Categoria.nueva(hogarId, sugeridas.get(i).nombre(), TipoCategoria.GASTO,
                        sugeridas.get(i).color(), sugeridas.get(i).icono(), Dinero.CERO, i))
                .toList());
    }

    private Categoria cargar(UUID hogarId, UUID categoriaId) {
        return categorias.porId(hogarId, categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", categoriaId));
    }

    private void verificarNombreLibre(Categoria c, UUID excluirId) {
        if (categorias.existeNombre(c.hogarId(), c.tipo(), c.nombre(), excluirId)) {
            throw new DomainException("CATEGORIA_DUPLICADA", "Ya tienes una categoría llamada «" + c.nombre() + "»");
        }
    }

    private static Dinero presupuesto(Comando c) {
        return c.presupuestoMensual() == null ? Dinero.CERO : Dinero.de(c.presupuestoMensual());
    }
}
