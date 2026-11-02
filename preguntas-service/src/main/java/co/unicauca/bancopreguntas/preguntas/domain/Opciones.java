package co.unicauca.bancopreguntas.preguntas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.List;

/**
 * Las cuatro opciones de respuesta de una pregunta de seleccion multiple (RF-05, RF-10).
 */
@Embeddable
public class Opciones {

    @Column(name = "opcion_a", nullable = false, length = 500)
    private String opcionA;

    @Column(name = "opcion_b", nullable = false, length = 500)
    private String opcionB;

    @Column(name = "opcion_c", nullable = false, length = 500)
    private String opcionC;

    @Column(name = "opcion_d", nullable = false, length = 500)
    private String opcionD;

    protected Opciones() {
        // requerido por JPA
    }

    public Opciones(String opcionA, String opcionB, String opcionC, String opcionD) {
        this.opcionA = opcionA;
        this.opcionB = opcionB;
        this.opcionC = opcionC;
        this.opcionD = opcionD;
    }

    /** Opciones en orden A, B, C, D (puede contener nulos si no se diligenciaron). */
    public List<String> comoLista() {
        return java.util.Arrays.asList(opcionA, opcionB, opcionC, opcionD);
    }

    public String getOpcionA() {
        return opcionA;
    }

    public String getOpcionB() {
        return opcionB;
    }

    public String getOpcionC() {
        return opcionC;
    }

    public String getOpcionD() {
        return opcionD;
    }
}
