package com.selahfinance.categorias.application.port.in;

import com.selahfinance.categorias.domain.model.Categoria;
import com.selahfinance.categorias.domain.model.TipoCategoria;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface GestionarCategoriasUseCase {

    /** Solo las activas del tipo indicado (o todas si {@code tipo} es null). */
    List<Categoria> listar(UUID hogarId, TipoCategoria tipo);

    Categoria crear(UUID hogarId, Comando comando);

    Categoria actualizar(UUID hogarId, UUID categoriaId, Comando comando);

    /** Se oculta, no se borra: los movimientos antiguos conservan su categoría. */
    void desactivar(UUID hogarId, UUID categoriaId);

    /** Crea las 7 categorías sugeridas si el hogar aún no tiene ninguna (idempotente). */
    void sembrarPorDefecto(UUID hogarId);

    record Comando(String nombre, TipoCategoria tipo, String color, String icono, BigDecimal presupuestoMensual) {
    }
}
