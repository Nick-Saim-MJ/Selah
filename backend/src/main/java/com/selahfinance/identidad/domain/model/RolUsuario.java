package com.selahfinance.identidad.domain.model;

public enum RolUsuario {
    /** Gestiona iglesias y cuentas. Sin hogar ni datos financieros. */
    ADMIN(false),
    /** Ve totales anónimos de su iglesia y los reportes que los hermanos decidan compartir. */
    PASTOR(true),
    /** Usuario normal: sus propias finanzas y hábitos. */
    HERMANO(true);

    private final boolean tieneFinanzas;

    RolUsuario(boolean tieneFinanzas) {
        this.tieneFinanzas = tieneFinanzas;
    }

    /** ¿Este rol tiene hogar (finanzas personales)? */
    public boolean tieneFinanzas() {
        return tieneFinanzas;
    }

    /** Solo se admite cambiar entre PASTOR y HERMANO (ambos tienen hogar). */
    public boolean puedeCambiarA(RolUsuario nuevo) {
        return this != ADMIN && nuevo != ADMIN;
    }
}
