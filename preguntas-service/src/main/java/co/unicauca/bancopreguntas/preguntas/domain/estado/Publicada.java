package co.unicauca.bancopreguntas.preguntas.domain.estado;

/** Publicada en el banco: disponible para simulacros. Solo puede archivarse. */
final class Publicada implements EstadoPregunta {

    @Override
    public CodigoEstado codigo() {
        return CodigoEstado.PUBLICADA;
    }

    @Override
    public EstadoPregunta archivar() {
        return CodigoEstado.ARCHIVADA.comportamiento();
    }
}
