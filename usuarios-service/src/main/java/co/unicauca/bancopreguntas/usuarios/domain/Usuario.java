package co.unicauca.bancopreguntas.usuarios.domain;

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
 * Usuario del sistema. La contrasena nunca se guarda en texto plano: solo su hash Argon2id.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String login;

    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;

    @Column(length = 120)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoUsuario estado;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro;

    protected Usuario() {
        // requerido por JPA
    }

    public Usuario(String login, String nombreCompleto, String email, Rol rol, String passwordHash) {
        this.login = login;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.rol = rol;
        this.estado = EstadoUsuario.ACTIVO;
        this.passwordHash = passwordHash;
        this.fechaRegistro = Instant.now();
    }

    public boolean estaActivo() {
        return estado == EstadoUsuario.ACTIVO;
    }

    public void cambiarEstado(EstadoUsuario nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public Rol getRol() {
        return rol;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }
}
