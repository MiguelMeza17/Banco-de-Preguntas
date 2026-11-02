package co.unicauca.bancopreguntas.preguntas.domain.estado;

/** Los revisores asignados la estan evaluando; termina aprobada o rechazada. */
final class EnRevision implements EstadoPregunta {

    @Override
    public CodigoEstado codigo() {
        return CodigoEstado.EN_REVISION;
    }

    @Override
    public EstadoPregunta aprobar() {
        return CodigoEstado.APROBADA.comportamiento();
    }

    @Override
    public EstadoPregunta rechazar() {
        return CodigoEstado.RECHAZADA.comportamiento();
    }
}
