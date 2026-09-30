package com.selahfinance.identidad.application.usecase;

import com.selahfinance.identidad.application.dto.SesionResult;
import com.selahfinance.identidad.application.port.out.TokenEmisorPort;
import com.selahfinance.identidad.domain.model.Usuario;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Arma la respuesta de sesión (token + datos que la app necesita para decidir qué pantallas mostrar). */
@Component
@RequiredArgsConstructor
class SesionEmisor {

    private final TokenEmisorPort tokens;

    SesionResult emitir(Usuario usuario, UUID hogarId) {
        var token = tokens.emitir(usuario.id(), hogarId, usuario.iglesiaId(), usuario.rol());
        return new SesionResult(token.token(), token.expiraEn(), usuario.id(), hogarId, usuario.iglesiaId(),
                usuario.rol(), usuario.nombres(), usuario.onboardingCompletado(), usuario.debeCambiarPassword());
    }
}
