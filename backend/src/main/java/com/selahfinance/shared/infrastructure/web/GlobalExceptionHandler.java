package com.selahfinance.shared.infrastructure.web;

import com.selahfinance.shared.application.exception.AccesoDenegadoException;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import com.selahfinance.shared.domain.DomainException;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Errores con formato estándar RFC 9457 (application/problem+json), igual para la app
 * móvil y para los sistemas de otros equipos que consumen la API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE_TIPO = "https://selahfinance.com/errores/";

    @ExceptionHandler(DomainException.class)
    ProblemDetail reglaDeNegocio(DomainException ex) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problema.setTitle("Regla de negocio incumplida");
        problema.setType(URI.create(BASE_TIPO + ex.codigo()));
        problema.setProperty("codigo", ex.codigo());
        return problema;
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ProblemDetail noEncontrado(RecursoNoEncontradoException ex) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Recurso no encontrado");
        return problema;
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    ProblemDetail accesoDenegado(AccesoDenegadoException ex) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problema.setTitle("Acceso denegado");
        return problema;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(e -> e.getField(), e -> String.valueOf(e.getDefaultMessage()), (a, b) -> a));
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Datos inválidos");
        problema.setTitle("Error de validación");
        problema.setProperty("campos", campos);
        return problema;
    }
}
