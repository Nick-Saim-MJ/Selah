package com.selahfinance.config;

import com.selahfinance.identidad.application.port.in.CrearAdminInicialUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Crea el primer administrador si se definen {@code SELAH_ADMIN_EMAIL} y {@code SELAH_ADMIN_PASSWORD}.
 * Es la única forma de tener un admin (no hay registro público de admins). Debe cambiar la contraseña
 * en su primer ingreso. Sin esas variables no hace nada.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
class AdminBootstrap implements ApplicationRunner {

    private final CrearAdminInicialUseCase admins;

    @Value("${selah.admin.email:}")
    private String email;

    @Value("${selah.admin.password:}")
    private String password;

    @Value("${selah.admin.nombres:Administrador}")
    private String nombres;

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        admins.crearSiNoExiste(email, password, nombres, true)
                .ifPresentOrElse(a -> log.info("Administrador inicial creado: {}", a.email()),
                        () -> log.debug("El administrador inicial ya existe: {}", email));
    }
}
