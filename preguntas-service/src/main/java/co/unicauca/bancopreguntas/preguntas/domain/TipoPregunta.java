package co.unicauca.bancopreguntas.preguntas.domain;

/**
 * Tipos de pregunta del banco. Cada tipo tiene su creador concreto (Factory Method); agregar un
 * tipo nuevo es agregar una constante y una subclase de {@code CreadorPregunta}.
 */
public enum TipoPregunta {
    SELECCION_MULTIPLE_UNICA_RESPUESTA
}
