package co.unicauca.bancopreguntas.preguntas.domain.fabrica;

import co.unicauca.bancopreguntas.preguntas.domain.Dificultad;
import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import co.unicauca.bancopreguntas.preguntas.domain.TipoPregunta;
import co.unicauca.bancopreguntas.preguntas.domain.validacion.ValidadorEstructural;

import java.util.List;
import java.util.Locale;

/**
 * Patron Factory Method (GoF, creacional). El algoritmo de creacion es comun a todos los tipos
 * de pregunta: construir, validar estructuralmente y entregar en Borrador. Lo unico que varia es
 * como se construye cada tipo, y esa decision se delega al metodo fabrica {@link #construir}
 * que implementa cada subclase.
 * <p>
 * Complementa al {@link Pregunta.Builder} del Corte 1: el Builder arma la pregunta paso a paso y
 * el creador decide que pasos aplican segun el tipo.
 */
public abstract class CreadorPregunta {

    private final ValidadorEstructural validador = new ValidadorEstructural();

    /** Tipo de pregunta que fabrica este creador. */
    public abstract TipoPregunta tipo();

    /** Metodo fabrica: construye la pregunta concreta a partir de los datos. */
    protected abstract Pregunta construir(FuenteDatosPregunta datos, Long autorId);

    /** Construye y valida; lanza {@link PreguntaInvalidaException} con todos los errores. */
    public final Pregunta crear(FuenteDatosPregunta datos, Long autorId) {
        Pregunta pregunta = construir(datos, autorId);
        List<String> errores = validar(pregunta);
        if (!errores.isEmpty()) {
            throw new PreguntaInvalidaException(errores);
        }
        return pregunta;
    }

    /** Validacion del tipo; las subclases pueden agregar reglas propias. */
    protected List<String> validar(Pregunta pregunta) {
        return validador.validar(pregunta);
    }

    protected static String limpiar(String valor) {
        return valor == null ? null : valor.strip();
    }

    /** Devuelve null si el texto no es una dificultad valida; el validador reporta el error. */
    protected static Dificultad dificultad(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Dificultad.valueOf(valor.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
