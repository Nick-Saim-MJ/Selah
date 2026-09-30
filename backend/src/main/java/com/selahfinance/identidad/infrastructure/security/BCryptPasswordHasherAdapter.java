package com.selahfinance.identidad.infrastructure.security;

import com.selahfinance.identidad.application.port.out.PasswordHasherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class BCryptPasswordHasherAdapter implements PasswordHasherPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String hashear(String passwordPlano) {
        return passwordEncoder.encode(passwordPlano);
    }

    @Override
    public boolean coincide(String passwordPlano, String hash) {
        return passwordPlano != null && passwordEncoder.matches(passwordPlano, hash);
    }
}
