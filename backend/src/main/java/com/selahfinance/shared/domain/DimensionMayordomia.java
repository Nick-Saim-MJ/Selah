package com.selahfinance.shared.domain;

/**
 * Las cuatro dimensiones de la mayordomía cristiana adventista.
 * Compartida por hábitos, reportes e integraciones con otros equipos.
 */
public enum DimensionMayordomia {
    TIEMPO("Tiempo", "Uso del tiempo como don de Dios: devoción, sábado, iglesia"),
    TALENTO("Talento", "Dones y habilidades puestos al servicio de otros"),
    TESORO("Tesoro", "Administración fiel de los recursos: diezmo, ofrenda, ahorro, deudas"),
    TEMPLO("Templo", "Cuidado del cuerpo como templo del Espíritu Santo");

    private final String nombre;
    private final String descripcion;

    DimensionMayordomia(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String nombre() {
        return nombre;
    }

    public String descripcion() {
        return descripcion;
    }
}
