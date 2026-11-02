package co.unicauca.bancopreguntas.preguntas.domain.validacion;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Validacion estructural automatica de una pregunta de seleccion multiple (HU03, RF-08 a RF-13).
 * Devuelve todos los errores encontrados para que el autor los corrija de una sola vez.
 */
public class ValidadorEstructural {

    public static final int LONGITUD_MINIMA_OPCION = 2;
    public static final int LONGITUD_MAXIMA_OPCION = 500;
    public static final List<String> EXPRESIONES_PROHIBIDAS = List.of(
            "todas las anteriores",
            "ninguna de las anteriores");
    private static final List<String> LETRAS = List.of("A", "B", "C", "D");

    public List<String> validar(Pregunta p) {
        List<String> errores = new ArrayList<>();
        obligatorio(p.getTitulo(), "El titulo es obligatorio.", errores);
        obligatorio(p.getContexto(), "El contexto es obligatorio (RF-08).", errores);
        obligatorio(p.getEnunciado(), "La pregunta directa es obligatoria (RF-09).", errores);
        obligatorio(p.getJustificacion(), "La justificacion de la respuesta es obligatoria.", errores);
        obligatorio(p.getBibliografia(), "La bibliografia es obligatoria.", errores);
        obligatorio(p.getCompetencia(), "La competencia es obligatoria.", errores);
        obligatorio(p.getTema(), "El tema es obligatorio.", errores);
        obligatorio(p.getSubtema(), "El subtema es obligatorio.", errores);
        if (p.getDificultad() == null) {
            errores.add("El nivel de dificultad es obligatorio.");
        }
        if (p.getEnunciado() != null && contarSignosDePregunta(p.getEnunciado()) > 1) {
            errores.add("Debe existir una unica pregunta directa (RF-09).");
        }
        validarOpciones(p, errores);
        return errores;
    }

    private void validarOpciones(Pregunta p, List<String> errores) {
        if (p.getOpciones() == null) {
            errores.add("Debe definir exactamente cuatro opciones de respuesta (RF-10).");
            return;
        }
        List<String> opciones = p.getOpciones().comoLista();
        Set<String> distintas = new HashSet<>();
        for (int i = 0; i < opciones.size(); i++) {
            String opcion = opciones.get(i);
            String letra = LETRAS.get(i);
            if (vacio(opcion)) {
                errores.add("La opcion " + letra + " no puede estar vacia (RF-10).");
                continue;
            }
            String texto = opcion.strip();
            if (texto.length() < LONGITUD_MINIMA_OPCION || texto.length() > LONGITUD_MAXIMA_OPCION) {
                errores.add("La opcion " + letra + " debe tener entre " + LONGITUD_MINIMA_OPCION
                        + " y " + LONGITUD_MAXIMA_OPCION + " caracteres (RF-13).");
            }
            String normalizada = normalizar(texto);
            for (String prohibida : EXPRESIONES_PROHIBIDAS) {
                if (normalizada.contains(prohibida)) {
                    errores.add("La opcion " + letra + " usa la expresion prohibida \"" + prohibida + "\" (RF-12).");
                }
            }
            if (!distintas.add(normalizada)) {
                errores.add("Las opciones no pueden repetirse (la opcion " + letra + " esta duplicada).");
            }
        }
        String clave = p.getRespuestaCorrecta();
        if (vacio(clave) || !LETRAS.contains(clave.strip().toUpperCase(Locale.ROOT))) {
            errores.add("Debe indicar una unica respuesta correcta: A, B, C o D (RF-11).");
        }
    }

    private static int contarSignosDePregunta(String texto) {
        return (int) texto.chars().filter(c -> c == '?').count();
    }

    private static void obligatorio(String valor, String mensaje, List<String> errores) {
        if (vacio(valor)) {
            errores.add(mensaje);
        }
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    /** Minusculas y sin tildes, para comparar sin importar como se escribio. */
    static String normalizar(String s) {
        String sinTildes = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").strip();
    }
}
