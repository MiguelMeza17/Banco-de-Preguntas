package co.unicauca.bancopreguntas.preguntas.evento;

/**
 * Contrato de mensajeria del sistema: topicos del bus y nombres logicos de los eventos.
 * Los nombres logicos viajan en la cabecera {@code _tipo} y desacoplan a cada servicio de las
 * clases Java de los demas: cada uno define su propia version del evento.
 */
public final class Topicos {

    public static final String PREGUNTA_CREADA = "banco.preguntas.creada";
    public static final String PREGUNTA_ENVIADA_A_REVISION = "banco.preguntas.enviada-revision";
    public static final String REVISORES_ASIGNADOS = "banco.revisiones.revisores-asignados";
    public static final String PREGUNTA_EVALUADA = "banco.revisiones.pregunta-evaluada";

    private Topicos() {
    }
}
