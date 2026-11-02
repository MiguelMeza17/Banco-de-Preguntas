package co.unicauca.bancopreguntas.preguntas.domain;

import co.unicauca.bancopreguntas.preguntas.domain.estado.CodigoEstado;
import co.unicauca.bancopreguntas.preguntas.domain.estado.EstadoPregunta;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.function.UnaryOperator;

/**
 * Pregunta del banco (RF-05). Es el contexto del patron State: guarda el codigo del estado y
 * delega en {@link EstadoPregunta} la decision de cada transicion.
 * <p>
 * Solo se construye con {@link Builder} (desde los creadores del Factory Method) y solo cambia
 * por medio de sus metodos de negocio: no expone setters.
 */
@Entity
@Table(name = "preguntas")
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoPregunta tipo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 4000)
    private String contexto;

    @Column(nullable = false, length = 1000)
    private String enunciado;

    @Embedded
    private Opciones opciones;

    @Column(name = "respuesta_correcta", nullable = false, length = 1)
    private String respuestaCorrecta;

    @Column(nullable = false, length = 2000)
    private String justificacion;

    @Column(nullable = false, length = 1000)
    private String bibliografia;

    @Column(nullable = false, length = 100)
    private String competencia;

    @Column(nullable = false, length = 100)
    private String tema;

    @Column(nullable = false, length = 100)
    private String subtema;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Dificultad dificultad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CodigoEstado estado;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    protected Pregunta() {
        // requerido por JPA
    }

    // ------------------------------------------------------------------ ciclo de vida (State)

    /** Solo el autor envia su pregunta a revision (HU-2: Borrador -> Pendiente de revision). */
    public void enviarARevision(Long usuarioId) {
        exigirAutor(usuarioId, "enviar a revision");
        transicionar(EstadoPregunta::enviarARevision);
    }

    /** Se asignaron revisores: Pendiente de revision -> En revision. */
    public void iniciarRevision() {
        transicionar(EstadoPregunta::iniciarRevision);
    }

    public void aprobar() {
        transicionar(EstadoPregunta::aprobar);
    }

    public void rechazar() {
        transicionar(EstadoPregunta::rechazar);
    }

    /** El autor retoma una pregunta rechazada para corregirla. */
    public void devolverABorrador(Long usuarioId) {
        exigirAutor(usuarioId, "devolver a borrador");
        transicionar(EstadoPregunta::devolverABorrador);
    }

    public void publicar() {
        transicionar(EstadoPregunta::publicar);
    }

    public void archivar() {
        transicionar(EstadoPregunta::archivar);
    }

    /**
     * Reemplaza el contenido con el de {@code cambios}, conservando id, autor, estado y fechas.
     * Solo el autor puede editar y solo si el estado lo permite (RF-06: en Borrador).
     */
    public void actualizarContenido(Pregunta cambios, Long usuarioId) {
        exigirAutor(usuarioId, "editar");
        if (!comportamiento().permiteEdicion()) {
            throw new ReglaNegocioException(
                    "Solo se puede editar una pregunta en estado Borrador; esta en " + estado.getNombre() + ".");
        }
        this.titulo = cambios.titulo;
        this.contexto = cambios.contexto;
        this.enunciado = cambios.enunciado;
        this.opciones = cambios.opciones;
        this.respuestaCorrecta = cambios.respuestaCorrecta;
        this.justificacion = cambios.justificacion;
        this.bibliografia = cambios.bibliografia;
        this.competencia = cambios.competencia;
        this.tema = cambios.tema;
        this.subtema = cambios.subtema;
        this.dificultad = cambios.dificultad;
        this.fechaActualizacion = Instant.now();
    }

    public EstadoPregunta comportamiento() {
        return estado.comportamiento();
    }

    private void transicionar(UnaryOperator<EstadoPregunta> accion) {
        this.estado = accion.apply(comportamiento()).codigo();
        this.fechaActualizacion = Instant.now();
    }

    private void exigirAutor(Long usuarioId, String accion) {
        if (usuarioId == null || !usuarioId.equals(autorId)) {
            throw new ReglaNegocioException("Solo el autor de la pregunta puede " + accion + ".");
        }
    }

    // ------------------------------------------------------------------ getters

    public Long getId() {
        return id;
    }

    public TipoPregunta getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getContexto() {
        return contexto;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public Opciones getOpciones() {
        return opciones;
    }

    public String getRespuestaCorrecta() {
        return respuestaCorrecta;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public String getBibliografia() {
        return bibliografia;
    }

    public String getCompetencia() {
        return competencia;
    }

    public String getTema() {
        return tema;
    }

    public String getSubtema() {
        return subtema;
    }

    public Dificultad getDificultad() {
        return dificultad;
    }

    public CodigoEstado getEstado() {
        return estado;
    }

    public Long getAutorId() {
        return autorId;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }

    // ------------------------------------------------------------------ Builder

    /**
     * Builder heredado del Corte 1. Lo usan los creadores concretos del Factory Method para
     * armar la pregunta paso a paso; toda pregunta nueva nace en Borrador.
     */
    public static class Builder {
        private final Pregunta p = new Pregunta();

        public Builder tipo(TipoPregunta tipo) {
            p.tipo = tipo;
            return this;
        }

        public Builder titulo(String titulo) {
            p.titulo = titulo;
            return this;
        }

        public Builder contexto(String contexto) {
            p.contexto = contexto;
            return this;
        }

        public Builder enunciado(String enunciado) {
            p.enunciado = enunciado;
            return this;
        }

        public Builder opciones(Opciones opciones) {
            p.opciones = opciones;
            return this;
        }

        public Builder respuestaCorrecta(String respuestaCorrecta) {
            p.respuestaCorrecta = respuestaCorrecta;
            return this;
        }

        public Builder justificacion(String justificacion) {
            p.justificacion = justificacion;
            return this;
        }

        public Builder bibliografia(String bibliografia) {
            p.bibliografia = bibliografia;
            return this;
        }

        public Builder competencia(String competencia) {
            p.competencia = competencia;
            return this;
        }

        public Builder tema(String tema) {
            p.tema = tema;
            return this;
        }

        public Builder subtema(String subtema) {
            p.subtema = subtema;
            return this;
        }

        public Builder dificultad(Dificultad dificultad) {
            p.dificultad = dificultad;
            return this;
        }

        public Builder autorId(Long autorId) {
            p.autorId = autorId;
            return this;
        }

        public Pregunta build() {
            Instant ahora = Instant.now();
            p.estado = CodigoEstado.BORRADOR;
            p.fechaCreacion = ahora;
            p.fechaActualizacion = ahora;
            return p;
        }
    }
}
