package com.selahfinance.integraciones.application.port.in;

import com.selahfinance.integraciones.domain.model.ClienteApi;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface GestionarClientesApiUseCase {

    /** Crea el cliente y su API key. La key en claro solo se devuelve aquí, una única vez. */
    ClienteCreado crear(String nombre, String descripcion, String responsableEmail, Set<String> scopes,
            Integer diasVigencia);

    List<ClienteApi> listar();

    /** Desactiva la key de inmediato. */
    void revocar(UUID clienteId);

    record ClienteCreado(ClienteApi cliente, String apiKey) {
    }
}
