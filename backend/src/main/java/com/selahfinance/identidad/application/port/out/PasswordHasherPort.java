package com.selahfinance.identidad.application.port.out;

public interface PasswordHasherPort {

    String hashear(String passwordPlano);

    boolean coincide(String passwordPlano, String hash);
}
