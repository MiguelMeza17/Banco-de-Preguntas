package co.unicauca.bancopreguntas.notificaciones.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Aviso para un usuario concreto ({@code usuarioId}) o para todos los de un rol
 * ({@code rol}, por ejemplo ADMINISTRADOR).
 */
@Entity
@Table(name = "notificaciones", indexes = {
        @Index(name = "ix_notif_usuario", columnList = "usuario_id"),
        @Index(name = "ix_notif_rol", columnList = "rol")})
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(length = 20)
    private String rol;

    @Column(name = "tipo_evento", nullable = false, length = 40)
    private String tipoEvento;

    @Column(name = "pregunta_id")
    private Long preguntaId;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String mensaje;

    @Column(nullable = false)
    private boolean leida;

    @Column(nullable = false)
    private Instant fecha;

    protected Notificacion() {
        // requerido por JPA
    }

    private Notificacion(Long usuarioId, String rol, String tipoEvento, Long preguntaId, String titulo, String mensaje) {
        this.usuarioId = usuarioId;
        this.rol = rol;
        this.tipoEvento = tipoEvento;
        this.preguntaId = preguntaId;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.leida = false;
        this.fecha = Instant.now();
    }

    public static Notificacion paraUsuario(Long usuarioId, String tipoEvento, Long preguntaId, String titulo, String mensaje) {
        return new Notificacion(usuarioId, null, tipoEvento, preguntaId, titulo, mensaje);
    }

    public static Notificacion paraRol(String rol, String tipoEvento, Long preguntaId, String titulo, String mensaje) {
        return new Notificacion(null, rol, tipoEvento, preguntaId, titulo, mensaje);
    }

    public void marcarLeida() {
        this.leida = true;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getRol() {
        return rol;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public Long getPreguntaId() {
        return preguntaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean isLeida() {
        return leida;
    }

    public Instant getFecha() {
        return fecha;
    }
}
