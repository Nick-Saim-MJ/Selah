package com.selahfinance.shared.infrastructure.web;

import com.selahfinance.shared.application.Pagina;
import java.util.List;
import java.util.function.Function;

public record PaginaResponse<T>(List<T> contenido, int pagina, int tamanio, long totalElementos, int totalPaginas) {

    public static <D, T> PaginaResponse<T> de(Pagina<D> pagina, Function<D, T> mapper) {
        var mapeada = pagina.map(mapper);
        return new PaginaResponse<>(mapeada.contenido(), mapeada.pagina(), mapeada.tamanio(),
                mapeada.totalElementos(), mapeada.totalPaginas());
    }
}
