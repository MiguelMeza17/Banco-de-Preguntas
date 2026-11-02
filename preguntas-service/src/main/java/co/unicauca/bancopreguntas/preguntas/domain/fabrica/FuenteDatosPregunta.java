package co.unicauca.bancopreguntas.preguntas.domain.fabrica;

import java.util.List;

/**
 * Datos que necesita un creador para construir una pregunta, sin importar de donde vengan
 * (peticion REST, archivo, otro servicio...). Es la interfaz objetivo (Target) del patron
 * Adapter: cada origen de datos se adapta a ella.
 */
public interface FuenteDatosPregunta {

    String tipo();

    String titulo();

    String contexto();

    String enunciado();

    /** Opciones A, B, C y D en ese orden. */
    List<String> opciones();

    /** Letra de la opcion correcta: A, B, C o D. */
    String respuestaCorrecta();

    String justificacion();

    String bibliografia();

    String competencia();

    String tema();

    String subtema();

    String dificultad();
}
