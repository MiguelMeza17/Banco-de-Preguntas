package co.unicauca.bancopreguntas.preguntas.domain.estado;

/** Estado inicial: el autor puede editar la pregunta, enviarla a revision o archivarla. */
final class Borrador implements EstadoPregunta {

    @Override
    public CodigoEstado codigo() {
        return CodigoEstado.BORRADOR;
    }

    @Override
    public boolean permiteEdicion() {
        return true;
    }

    @Override
    public EstadoPregunta enviarARevision() {
        return CodigoEstado.PENDIENTE_REVISION.comportamiento();
    }

    @Override
    public EstadoPregunta archivar() {
        return CodigoEstado.ARCHIVADA.comportamiento();
    }
}
