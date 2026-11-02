package co.unicauca.bancopreguntas.notificaciones.service;

import co.unicauca.bancopreguntas.notificaciones.domain.Notificacion;
import co.unicauca.bancopreguntas.notificaciones.domain.PreguntaConocida;
import co.unicauca.bancopreguntas.notificaciones.evento.Eventos;
import co.unicauca.bancopreguntas.notificaciones.repository.NotificacionRepository;
import co.unicauca.bancopreguntas.notificaciones.repository.PreguntaConocidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Decide a quien avisar ante cada evento del sistema. Es el observador que reemplaza a la
 * notificacion directa del Corte 1: los demas servicios solo publican eventos.
 */
@Service
public class NotificacionService {

    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private final NotificacionRepository notificaciones;
    private final PreguntaConocidaRepository preguntas;
    private final CanalNotificacion canal;

    public NotificacionService(NotificacionRepository notificaciones, PreguntaConocidaRepository preguntas,
                               CanalNotificacion canal) {
        this.notificaciones = notificaciones;
        this.preguntas = preguntas;
        this.canal = canal;
    }

    @Transactional
    public void alCrearPregunta(Eventos.PreguntaCreada e) {
        preguntas.save(new PreguntaConocida(e.preguntaId(), e.autorId(), e.titulo()));
    }

    @Transactional
    public List<Notificacion> alEnviarARevision(Eventos.PreguntaEnviadaARevision e) {
        preguntas.save(new PreguntaConocida(e.preguntaId(), e.autorId(), e.titulo()));
        return guardar(List.of(Notificacion.paraRol(ROL_ADMINISTRADOR, "PreguntaEnviadaARevision", e.preguntaId(),
                "Pregunta pendiente de revision",
                "La pregunta \"" + e.titulo() + "\" espera la asignacion de revisores.")));
    }

    @Transactional
    public List<Notificacion> alAsignarRevisores(Eventos.RevisoresAsignados e) {
        String titulo = tituloDe(e.preguntaId());
        List<Notificacion> nuevas = new ArrayList<>();
        for (Long revisorId : e.revisorIds()) {
            nuevas.add(Notificacion.paraUsuario(revisorId, "RevisoresAsignados", e.preguntaId(),
                    "Nueva pregunta para revisar",
                    "Se le asigno la revision de " + titulo + ". Encuentrela en \"Mis revisiones\"."));
        }
        return guardar(nuevas);
    }

    /** Avisa al autor y al administrador; si ya hay dictamen final, lo informa. */
    @Transactional
    public List<Notificacion> alEvaluarPregunta(Eventos.PreguntaEvaluada e) {
        String titulo = tituloDe(e.preguntaId());
        String dictamen = "El revisor " + e.revisorId() + " marco " + titulo + " como "
                + e.resultado().toLowerCase() + "."
                + (e.observaciones() == null ? "" : " Observaciones: " + e.observaciones());
        String cierre = e.revisionCompleta()
                ? " Revision completa: la pregunta quedo " + e.resultadoPregunta().toLowerCase() + "."
                : " Aun faltan revisores por evaluar.";

        List<Notificacion> nuevas = new ArrayList<>();
        Set<String> destinatarios = e.destinatarios() == null ? Set.of("AUTOR", ROL_ADMINISTRADOR) : e.destinatarios();
        Optional<PreguntaConocida> pregunta = preguntas.findById(e.preguntaId());
        if (destinatarios.contains("AUTOR") && pregunta.isPresent()) {
            nuevas.add(Notificacion.paraUsuario(pregunta.get().getAutorId(), "PreguntaEvaluada", e.preguntaId(),
                    "Su pregunta fue evaluada", dictamen + cierre));
        }
        if (destinatarios.contains(ROL_ADMINISTRADOR)) {
            nuevas.add(Notificacion.paraRol(ROL_ADMINISTRADOR, "PreguntaEvaluada", e.preguntaId(),
                    "Pregunta evaluada", dictamen + cierre));
        }
        return guardar(nuevas);
    }

    @Transactional(readOnly = true)
    public List<Notificacion> bandeja(Long usuarioId, String rol) {
        return notificaciones.bandeja(usuarioId, rol == null || rol.isBlank() ? null : rol.toUpperCase());
    }

    @Transactional
    public Notificacion marcarLeida(Long id) {
        Notificacion n = notificaciones.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la notificacion " + id + "."));
        n.marcarLeida();
        return notificaciones.save(n);
    }

    private String tituloDe(Long preguntaId) {
        return preguntas.findById(preguntaId)
                .map(p -> "la pregunta \"" + p.getTitulo() + "\"")
                .orElse("la pregunta " + preguntaId);
    }

    private List<Notificacion> guardar(List<Notificacion> nuevas) {
        List<Notificacion> guardadas = notificaciones.saveAll(nuevas);
        guardadas.forEach(canal::enviar);
        return guardadas;
    }
}
