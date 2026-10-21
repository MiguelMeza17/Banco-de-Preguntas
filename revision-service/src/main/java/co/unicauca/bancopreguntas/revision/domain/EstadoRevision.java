package co.unicauca.bancopreguntas.revision.domain;

import java.util.Set;

/**
 * Estados por los que atraviesa la revision de una pregunta asignada a un revisor.
 * APROBADA y RECHAZADA son estados finales: una vez alcanzados, la revision no
 * admite nuevas transiciones.
 */
public enum EstadoRevision {
    ASIGNADA,
    EN_REVISION,
    APROBADA,
    RECHAZADA;

    private static final Set<EstadoRevision> ESTADOS_FINALES = Set.of(APROBADA, RECHAZADA);

    public boolean puedeTransicionarA(EstadoRevision nuevoEstado) {
        if (ESTADOS_FINALES.contains(this)) {
            return false;
        }
        return switch (this) {
            case ASIGNADA -> nuevoEstado == EN_REVISION || nuevoEstado == APROBADA || nuevoEstado == RECHAZADA;
            case EN_REVISION -> nuevoEstado == APROBADA || nuevoEstado == RECHAZADA;
            default -> false;
        };
    }
}
