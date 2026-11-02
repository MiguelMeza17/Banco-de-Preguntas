package co.unicauca.bancopreguntas.revision.evento;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Evento de dominio: se asignaron revisores a una pregunta.
 * <p>
 * Reemplaza la notificacion directa: el servicio de revision solo anuncia lo que paso
 * y el servicio de notificaciones (u otro interesado) decide como avisar a cada revisor.
 *
 * @param preguntaId  pregunta que se va a revisar
 * @param revisionIds identificadores de las revisiones creadas
 * @param revisorIds  revisores asignados, en el mismo orden que {@code revisionIds}
 * @param ocurridoEn  momento de la asignacion
 */
public record RevisoresAsignados(
        Long preguntaId,
        List<Long> revisionIds,
        List<Long> revisorIds,
        Instant ocurridoEn
) implements EventoIntegracion {
    public RevisoresAsignados {
        revisionIds = Collections.unmodifiableList(new ArrayList<>(revisionIds));
        revisorIds = Collections.unmodifiableList(new ArrayList<>(revisorIds));
    }

    @Override
    public String topico() {
        return Topicos.REVISORES_ASIGNADOS;
    }
}
