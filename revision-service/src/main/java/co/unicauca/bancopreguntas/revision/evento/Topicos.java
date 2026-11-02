package co.unicauca.bancopreguntas.revision.evento;

/** Topicos del bus de eventos que usa este servicio (ver docs/corte2/EVENTOS.md). */
public final class Topicos {

    public static final String REVISORES_ASIGNADOS = "banco.revisiones.revisores-asignados";
    public static final String PREGUNTA_EVALUADA = "banco.revisiones.pregunta-evaluada";

    private Topicos() {
    }
}
