package com.selahfinance.iglesias.application.port.in;

import com.selahfinance.iglesias.domain.model.Iglesia;
import java.util.UUID;

public interface GestionarIglesiasUseCase {

    Iglesia crear(String nombre, String ciudad, String distrito);

    Iglesia actualizar(UUID id, String nombre, String ciudad, String distrito, boolean activa);
}
