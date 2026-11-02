package co.unicauca.bancopreguntas.revision.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Asignacion de un revisor a una pregunta del banco. El servicio de revision
 * mantiene su propia persistencia (no depende del almacenamiento de preguntas
 * ni de usuarios de otros servicios), solo referencia sus identificadores.
 */
@Entity
@Table(name = "revisiones")
public class Revision {

    public static final int MAX_OBSERVACIONES = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pregunta_id", nullable = false)
    private Long preguntaId;

    @Column(name = "revisor_id", nullable = false)
    private Long revisorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoRevision estado;

    @Column(name = "fecha_asignacion", nullable = false)
    private Instant fechaAsignacion;

    @Column(name = "observaciones", length = MAX_OBSERVACIONES)
    private String observaciones;

    @Column(name = "fecha_evaluacion")
    private Instant fechaEvaluacion;

    protected Revision() {
        // requerido por JPA
    }

    public Revision(Long preguntaId, Long revisorId) {
        this.preguntaId = preguntaId;
        this.revisorId = revisorId;
        this.estado = EstadoRevision.ASIGNADA;
        this.fechaAsignacion = Instant.now();
    }

    /**
     * Registra el dictamen del revisor.
     * <ul>
     *     <li>Solo el revisor asignado puede evaluar.</li>
     *     <li>El resultado debe ser APROBADA o RECHAZADA y la transicion debe ser valida
     *     (una revision ya evaluada no se puede volver a evaluar).</li>
     *     <li>Un rechazo exige observaciones para que el autor sepa que corregir.</li>
     * </ul>
     *
     * @param revisorEvaluador revisor que emite el dictamen
     * @param resultado        APROBADA o RECHAZADA
     * @param observaciones    comentarios para el autor (obligatorios al rechazar)
     */
    public void evaluar(Long revisorEvaluador, EstadoRevision resultado, String observaciones) {
        if (revisorEvaluador == null || !revisorEvaluador.equals(revisorId)) {
            throw new ReglaRevisionException("Solo el revisor asignado puede evaluar esta revision.");
        }
        if (resultado != EstadoRevision.APROBADA && resultado != EstadoRevision.RECHAZADA) {
            throw new ReglaRevisionException("El resultado de la evaluacion debe ser APROBADA o RECHAZADA.");
        }
        if (!estado.puedeTransicionarA(resultado)) {
            throw new ReglaRevisionException(
                    "La revision esta en estado " + estado + " y no puede pasar a " + resultado + ".");
        }
        String texto = observaciones == null ? null : observaciones.strip();
        if (resultado == EstadoRevision.RECHAZADA && (texto == null || texto.isEmpty())) {
            throw new ReglaRevisionException("Un rechazo debe incluir observaciones para el autor.");
        }
        if (texto != null && texto.length() > MAX_OBSERVACIONES) {
            throw new ReglaRevisionException(
                    "Las observaciones no pueden superar " + MAX_OBSERVACIONES + " caracteres.");
        }
        this.estado = resultado;
        this.observaciones = texto == null || texto.isEmpty() ? null : texto;
        this.fechaEvaluacion = Instant.now();
    }

    /**
     * El revisor abre la pregunta para leerla: ASIGNADA pasa a EN_REVISION.
     * Si ya estaba en revision no cambia nada (abrirla dos veces no es un error).
     */
    public void iniciar(Long revisorQueAbre) {
        if (revisorQueAbre == null || !revisorQueAbre.equals(revisorId)) {
            throw new ReglaRevisionException("Solo el revisor asignado puede abrir esta revision.");
        }
        if (estado == EstadoRevision.EN_REVISION) {
            return;
        }
        if (!estado.puedeTransicionarA(EstadoRevision.EN_REVISION)) {
            throw new ReglaRevisionException("La revision ya fue evaluada (" + estado + ").");
        }
        this.estado = EstadoRevision.EN_REVISION;
    }

    /** Indica si el revisor ya emitio su dictamen (APROBADA o RECHAZADA). */
    public boolean tieneDictamen() {
        return estado == EstadoRevision.APROBADA || estado == EstadoRevision.RECHAZADA;
    }

    public Long getId() {
        return id;
    }

    public Long getPreguntaId() {
        return preguntaId;
    }

    public Long getRevisorId() {
        return revisorId;
    }

    public EstadoRevision getEstado() {
        return estado;
    }

    public Instant getFechaAsignacion() {
        return fechaAsignacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public Instant getFechaEvaluacion() {
        return fechaEvaluacion;
    }
}
