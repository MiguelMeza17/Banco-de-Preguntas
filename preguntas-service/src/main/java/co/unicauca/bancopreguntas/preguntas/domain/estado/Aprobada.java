package co.unicauca.bancopreguntas.preguntas.domain.estado;

/** Aprobada por los revisores: lista para publicarse en el banco o archivarse. */
final class Aprobada implements EstadoPregunta {

    @Override
    public CodigoEstado codigo() {
        return CodigoEstado.APROBADA;
    }

    @Override
    public EstadoPregunta publicar() {
        return CodigoEstado.PUBLICADA.comportamiento();
    }

    @Override
    public EstadoPregunta archivar() {
        return CodigoEstado.ARCHIVADA.comportamiento();
    }
}
