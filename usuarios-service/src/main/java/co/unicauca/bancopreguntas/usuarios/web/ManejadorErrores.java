package co.unicauca.bancopreguntas.usuarios.web;

import co.unicauca.bancopreguntas.usuarios.service.UsuarioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(UsuarioException.class)
    public ProblemDetail usuario(UsuarioException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(e.getEstadoHttp()), e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail cuerpoInvalido(HttpMessageNotReadableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no es valido.");
    }
}
