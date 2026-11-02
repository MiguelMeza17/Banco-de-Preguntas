package co.unicauca.bancopreguntas.preguntas.domain.fabrica;

import co.unicauca.bancopreguntas.preguntas.domain.Opciones;
import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import co.unicauca.bancopreguntas.preguntas.domain.TipoPregunta;

import java.util.List;
import java.util.Locale;

/**
 * Creador concreto para preguntas de seleccion multiple con unica respuesta (formato Saber Pro):
 * contexto, enunciado, cuatro opciones y la letra de la correcta.
 */
public class CreadorPreguntaSeleccionMultiple extends CreadorPregunta {

    @Override
    public TipoPregunta tipo() {
        return TipoPregunta.SELECCION_MULTIPLE_UNICA_RESPUESTA;
    }

    @Override
    protected Pregunta construir(FuenteDatosPregunta datos, Long autorId) {
        List<String> o = datos.opciones() == null ? List.of() : datos.opciones();
        Opciones opciones = o.size() == 4
                ? new Opciones(limpiar(o.get(0)), limpiar(o.get(1)), limpiar(o.get(2)), limpiar(o.get(3)))
                : null;
        String clave = datos.respuestaCorrecta() == null ? null
                : datos.respuestaCorrecta().strip().toUpperCase(Locale.ROOT);

        return new Pregunta.Builder()
                .tipo(tipo())
                .titulo(limpiar(datos.titulo()))
                .contexto(limpiar(datos.contexto()))
                .enunciado(limpiar(datos.enunciado()))
                .opciones(opciones)
                .respuestaCorrecta(clave)
                .justificacion(limpiar(datos.justificacion()))
                .bibliografia(limpiar(datos.bibliografia()))
                .competencia(limpiar(datos.competencia()))
                .tema(limpiar(datos.tema()))
                .subtema(limpiar(datos.subtema()))
                .dificultad(dificultad(datos.dificultad()))
                .autorId(autorId)
                .build();
    }
}
