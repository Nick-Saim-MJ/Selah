package com.selahfinance.identidad.infrastructure.security;

import com.selahfinance.identidad.application.port.out.TokenEmisorPort;
import com.selahfinance.identidad.domain.model.RolUsuario;
import com.selahfinance.shared.infrastructure.security.SeguridadProperties;
import com.selahfinance.shared.infrastructure.web.UsuarioActual;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JwtTokenEmisorAdapter implements TokenEmisorPort {

    private final JwtEncoder encoder;
    private final SeguridadProperties props;
    private final Clock clock;

    @Override
    public TokenEmitido emitir(UUID usuarioId, UUID hogarId, UUID iglesiaId, RolUsuario rol) {
        Instant ahora = clock.instant();
        Instant expira = ahora.plus(props.jwtExpiracion());
        var claims = JwtClaimsSet.builder()
                .issuer(props.jwtEmisor())
                .subject(usuarioId.toString())
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim(UsuarioActual.CLAIM_ROL, rol.name());
        if (hogarId != null) {
            claims.claim(UsuarioActual.CLAIM_HOGAR, hogarId.toString());
        }
        if (iglesiaId != null) {
            claims.claim(UsuarioActual.CLAIM_IGLESIA, iglesiaId.toString());
        }
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new TokenEmitido(token, expira);
    }
}
