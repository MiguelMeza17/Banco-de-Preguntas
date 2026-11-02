package co.unicauca.bancopreguntas.preguntas.domain.estado;

/** Estado final: la pregunta se conserva para la trazabilidad pero no admite mas acciones. */
final class Archivada implements EstadoPregunta {

    @Override
    public CodigoEstado codigo() {
        return CodigoEstado.ARCHIVADA;
    }
}
