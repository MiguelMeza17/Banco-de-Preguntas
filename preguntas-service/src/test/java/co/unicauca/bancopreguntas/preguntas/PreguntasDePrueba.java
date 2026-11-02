package co.unicauca.bancopreguntas.preguntas;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import co.unicauca.bancopreguntas.preguntas.domain.fabrica.CreadorPreguntaSeleccionMultiple;
import co.unicauca.bancopreguntas.preguntas.web.adaptador.PreguntaRequestAdapter;
import co.unicauca.bancopreguntas.preguntas.web.dto.PreguntaRequest;

/** Datos de prueba reutilizables. */
public final class PreguntasDePrueba {

    public static final Long AUTOR = 2L;

    private PreguntasDePrueba() {
    }

    public static PreguntaRequest valida() {
        return conTemaYCompetencia("Estadistica", "Razonamiento cuantitativo", "MEDIA", "Lectura de graficas");
    }

    public static PreguntaRequest conTemaYCompetencia(String tema, String competencia, String dificultad, String titulo) {
        return new PreguntaRequest(AUTOR, null, titulo,
                "La grafica muestra la produccion de cafe del Cauca entre 2018 y 2023.",
                "Segun la grafica, ¿en que ano se produjo el mayor crecimiento?",
                "2019", "2020", "2021", "2022", "c",
                "Entre 2020 y 2021 la produccion paso de 40 a 65 toneladas, el mayor aumento del periodo.",
                "DANE (2024). Encuesta nacional agropecuaria.",
                competencia, tema, "Interpretacion de graficas", dificultad);
    }

    public static Pregunta pregunta(PreguntaRequest request) {
        return new CreadorPreguntaSeleccionMultiple().crear(new PreguntaRequestAdapter(request), request.usuarioId());
    }

    public static Pregunta pregunta() {
        return pregunta(valida());
    }
}
