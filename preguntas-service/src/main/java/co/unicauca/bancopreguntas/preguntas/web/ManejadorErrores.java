package co.unicauca.bancopreguntas.preguntas.web;

import co.unicauca.bancopreguntas.preguntas.domain.ReglaNegocioException;
import co.unicauca.bancopreguntas.preguntas.domain.fabrica.PreguntaInvalidaException;
import co.unicauca.bancopreguntas.preguntas.service.PreguntaNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Traduce las excepciones a respuestas HTTP con cuerpo {@link ProblemDetail}. */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(PreguntaNoEncontradaException.class)
    public ProblemDetail noEncontrada(PreguntaNoEncontradaException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(PreguntaInvalidaException.class)
    public ProblemDetail invalida(PreguntaInvalidaException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "La pregunta no cumple la validacion estructural.");
        problema.setProperty("errores", e.getErrores());
        return problema;
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail regla(ReglaNegocioException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail peticionInvalida(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "La peticion tiene un valor no valido (revise estado, dificultad y numeros).");
    }
}
