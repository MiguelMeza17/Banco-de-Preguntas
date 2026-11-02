package co.unicauca.bancopreguntas.preguntas.web.adaptador;

import co.unicauca.bancopreguntas.preguntas.domain.fabrica.FuenteDatosPregunta;
import co.unicauca.bancopreguntas.preguntas.web.dto.PreguntaRequest;

import java.util.Arrays;
import java.util.List;

/**
 * Patron Adapter (GoF, estructural). El DTO REST {@link PreguntaRequest} (adaptado) tiene la forma
 * que conviene al cliente web: campos planos opcionA..opcionD. El dominio espera una
 * {@link FuenteDatosPregunta} (objetivo) con las opciones como lista. Este adaptador envuelve el
 * DTO y lo expone con la interfaz del dominio, sin que ninguno de los dos conozca al otro.
 */
public class PreguntaRequestAdapter implements FuenteDatosPregunta {

    private final PreguntaRequest request;

    public PreguntaRequestAdapter(PreguntaRequest request) {
        this.request = request;
    }

    @Override
    public String tipo() {
        return request.tipo();
    }

    @Override
    public String titulo() {
        return request.titulo();
    }

    @Override
    public String contexto() {
        return request.contexto();
    }

    @Override
    public String enunciado() {
        return request.enunciado();
    }

    @Override
    public List<String> opciones() {
        return Arrays.asList(request.opcionA(), request.opcionB(), request.opcionC(), request.opcionD());
    }

    @Override
    public String respuestaCorrecta() {
        return request.respuestaCorrecta();
    }

    @Override
    public String justificacion() {
        return request.justificacion();
    }

    @Override
    public String bibliografia() {
        return request.bibliografia();
    }

    @Override
    public String competencia() {
        return request.competencia();
    }

    @Override
    public String tema() {
        return request.tema();
    }

    @Override
    public String subtema() {
        return request.subtema();
    }

    @Override
    public String dificultad() {
        return request.dificultad();
    }
}
