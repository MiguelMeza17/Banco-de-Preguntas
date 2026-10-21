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

    protected Revision() {
        // requerido por JPA
    }

    public Revision(Long preguntaId, Long revisorId) {
        this.preguntaId = preguntaId;
        this.revisorId = revisorId;
        this.estado = EstadoRevision.ASIGNADA;
        this.fechaAsignacion = Instant.now();
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
}
