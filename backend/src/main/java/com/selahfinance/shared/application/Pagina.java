package com.selahfinance.shared.application;

import java.util.List;
import java.util.function.Function;

/** Resultado paginado independiente de Spring Data. */
public record Pagina<T>(List<T> contenido, int pagina, int tamanio, long totalElementos) {

    public int totalPaginas() {
        return tamanio == 0 ? 0 : (int) Math.ceil((double) totalElementos / tamanio);
    }

    public <R> Pagina<R> map(Function<T, R> mapper) {
        return new Pagina<>(contenido.stream().map(mapper).toList(), pagina, tamanio, totalElementos);
    }
}
