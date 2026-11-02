package co.unicauca.bancopreguntas.notificaciones.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Proyeccion local de las preguntas, construida a partir del evento PreguntaCreada.
 * Permite saber quien es el autor de una pregunta sin consultar a preguntas-service
 * (cada servicio es autonomo y no depende de que el otro este disponible).
 */
@Entity
@Table(name = "preguntas_conocidas")
public class PreguntaConocida {

    @Id
    private Long preguntaId;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(nullable = false, length = 150)
    private String titulo;

    protected PreguntaConocida() {
        // requerido por JPA
    }

    public PreguntaConocida(Long preguntaId, Long autorId, String titulo) {
        this.preguntaId = preguntaId;
        this.autorId = autorId;
        this.titulo = titulo;
    }

    public Long getPreguntaId() {
        return preguntaId;
    }

    public Long getAutorId() {
        return autorId;
    }

    public String getTitulo() {
        return titulo;
    }
}
