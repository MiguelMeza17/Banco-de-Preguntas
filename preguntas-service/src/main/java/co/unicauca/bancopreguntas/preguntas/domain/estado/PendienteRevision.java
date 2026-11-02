package co.unicauca.bancopreguntas.preguntas.domain.estado;

/** Esperando que el administrador asigne revisores; al asignarlos pasa a En revision. */
final class PendienteRevision implements EstadoPregunta {

    @Override
    public CodigoEstado codigo() {
        return CodigoEstado.PENDIENTE_REVISION;
    }

    @Override
    public EstadoPregunta iniciarRevision() {
        return CodigoEstado.EN_REVISION.comportamiento();
    }
}
