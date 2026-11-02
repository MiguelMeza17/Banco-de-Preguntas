package co.unicauca.bancopreguntas.preguntas.domain.estado;

/**
 * Identificador persistible de cada estado (RF-14). Se guarda en la base de datos y se traduce
 * al objeto {@link EstadoPregunta} que contiene el comportamiento.
 */
public enum CodigoEstado {
    BORRADOR("Borrador", new Borrador()),
    PENDIENTE_REVISION("Pendiente de revision", new PendienteRevision()),
    EN_REVISION("En revision", new EnRevision()),
    APROBADA("Aprobada", new Aprobada()),
    RECHAZADA("Rechazada", new Rechazada()),
    PUBLICADA("Publicada", new Publicada()),
    ARCHIVADA("Archivada", new Archivada());

    private final String nombre;
    private final EstadoPregunta estado;

    CodigoEstado(String nombre, EstadoPregunta estado) {
        this.nombre = nombre;
        this.estado = estado;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoPregunta comportamiento() {
        return estado;
    }
}
