package com.selahfinance.identidad.application.dto;

import com.selahfinance.identidad.domain.model.Usuario;

/** Usuario con el nombre de su iglesia ya resuelto, para listados y perfil. */
public record UsuarioDetalle(Usuario usuario, String iglesiaNombre) {
}
