package com.selahfinance.identidad.application.port.in;

import com.selahfinance.identidad.application.dto.SesionResult;

public interface IniciarSesionUseCase {

    SesionResult iniciarSesion(String email, String password);
}
