package co.unicauca.bancopreguntas.revision.web;

import co.unicauca.bancopreguntas.revision.domain.ReglaRevisionException;
import co.unicauca.bancopreguntas.revision.service.RevisionNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones del servicio a respuestas HTTP con cuerpo {@link ProblemDetail}.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RevisionNoEncontradaException.class)
    public ProblemDetail noEncontrada(RevisionNoEncontradaException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ReglaRevisionException.class)
    public ProblemDetail reglaIncumplida(ReglaRevisionException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }
}
