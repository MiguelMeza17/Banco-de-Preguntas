package co.unicauca.bancopreguntas.preguntas.domain.estado;

/** Rechazada: el autor la devuelve a Borrador para corregirla segun las observaciones, o se archiva. */
final class Rechazada implements EstadoPregunta {

    @Override
    public CodigoEstado codigo() {
        return CodigoEstado.RECHAZADA;
    }

    @Override
    public EstadoPregunta devolverABorrador() {
        return CodigoEstado.BORRADOR.comportamiento();
    }

    @Override
    public EstadoPregunta archivar() {
        return CodigoEstado.ARCHIVADA.comportamiento();
    }
}
