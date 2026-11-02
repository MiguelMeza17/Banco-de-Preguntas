package co.unicauca.bancopreguntas.preguntas.domain.estado;

import co.unicauca.bancopreguntas.preguntas.domain.ReglaNegocioException;

/**
 * Patron State (GoF, comportamiento): cada estado del ciclo de vida de una pregunta (RF-14)
 * decide que acciones admite y a que estado lleva cada una (RF-15).
 * <p>
 * Por defecto toda accion es invalida; cada estado concreto sobrescribe solo las transiciones
 * que permite. Asi agregar un estado nuevo es agregar una clase, sin cadenas de {@code if}
 * sobre un String como en el Corte 1.
 */
public interface EstadoPregunta {

    CodigoEstado codigo();

    /** Indica si el contenido de la pregunta se puede modificar en este estado (RF-06). */
    default boolean permiteEdicion() {
        return false;
    }

    default EstadoPregunta enviarARevision() {
        throw invalida("enviar a revision");
    }

    default EstadoPregunta iniciarRevision() {
        throw invalida("iniciar la revision");
    }

    default EstadoPregunta aprobar() {
        throw invalida("aprobar");
    }

    default EstadoPregunta rechazar() {
        throw invalida("rechazar");
    }

    default EstadoPregunta devolverABorrador() {
        throw invalida("devolver a borrador");
    }

    default EstadoPregunta publicar() {
        throw invalida("publicar");
    }

    default EstadoPregunta archivar() {
        throw invalida("archivar");
    }

    private ReglaNegocioException invalida(String accion) {
        return new TransicionInvalidaException(
                "No se puede " + accion + " una pregunta en estado " + codigo().getNombre() + ".");
    }
}
