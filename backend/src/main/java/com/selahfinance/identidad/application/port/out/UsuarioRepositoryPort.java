package com.selahfinance.identidad.application.port.out;

import com.selahfinance.identidad.application.port.in.GestionarUsuariosUseCase.Filtro;
import com.selahfinance.identidad.domain.model.EstadoUsuario;
import com.selahfinance.identidad.domain.model.RolUsuario;
import com.selahfinance.identidad.domain.model.Usuario;
import com.selahfinance.shared.application.Pagina;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositoryPort {

    boolean existeEmail(String email);

    Optional<Usuario> porId(UUID id);

    Optional<Usuario> porEmail(String email);

    Usuario guardar(Usuario usuario);

    Pagina<Usuario> buscar(Filtro filtro);

    List<Conteo> contarPorRolYEstado();

    record Conteo(RolUsuario rol, EstadoUsuario estado, long cantidad) {
    }
}
