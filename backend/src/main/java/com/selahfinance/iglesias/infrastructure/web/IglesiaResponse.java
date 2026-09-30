package com.selahfinance.iglesias.infrastructure.web;

import com.selahfinance.iglesias.domain.model.Iglesia;
import java.util.UUID;

record IglesiaResponse(UUID id, String nombre, String ciudad, String distrito, boolean activa) {

    static IglesiaResponse de(Iglesia i) {
        return new IglesiaResponse(i.id(), i.nombre(), i.ciudad(), i.distrito(), i.activa());
    }
}
