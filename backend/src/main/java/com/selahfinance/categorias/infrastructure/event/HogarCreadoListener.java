package com.selahfinance.categorias.infrastructure.event;

import com.selahfinance.categorias.application.port.in.GestionarCategoriasUseCase;
import com.selahfinance.hogar.domain.event.HogarCreado;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Todo hogar nuevo arranca con las 7 categorías sugeridas (editables después). */
@Component
@RequiredArgsConstructor
class HogarCreadoListener {

    private final GestionarCategoriasUseCase categorias;

    @EventListener
    void alCrearHogar(HogarCreado evento) {
        categorias.sembrarPorDefecto(evento.hogarId());
    }
}
