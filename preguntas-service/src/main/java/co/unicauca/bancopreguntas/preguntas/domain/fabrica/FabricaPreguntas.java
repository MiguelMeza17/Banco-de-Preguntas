package co.unicauca.bancopreguntas.preguntas.domain.fabrica;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import co.unicauca.bancopreguntas.preguntas.domain.ReglaNegocioException;
import co.unicauca.bancopreguntas.preguntas.domain.TipoPregunta;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Elige el creador concreto segun el tipo solicitado. Para soportar un tipo nuevo basta con
 * registrar su creador; ni esta clase ni el servicio cambian (escenario de modificabilidad).
 */
public class FabricaPreguntas {

    private final Map<TipoPregunta, CreadorPregunta> creadores = new EnumMap<>(TipoPregunta.class);

    public FabricaPreguntas(List<CreadorPregunta> creadores) {
        creadores.forEach(c -> this.creadores.put(c.tipo(), c));
    }

    public Pregunta crear(FuenteDatosPregunta datos, Long autorId) {
        return creadorPara(datos.tipo()).crear(datos, autorId);
    }

    CreadorPregunta creadorPara(String tipo) {
        TipoPregunta tipoPregunta;
        if (tipo == null || tipo.isBlank()) {
            tipoPregunta = TipoPregunta.SELECCION_MULTIPLE_UNICA_RESPUESTA;
        } else {
            try {
                tipoPregunta = TipoPregunta.valueOf(tipo.strip().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new ReglaNegocioException("Tipo de pregunta no soportado: " + tipo + ".");
            }
        }
        CreadorPregunta creador = creadores.get(tipoPregunta);
        if (creador == null) {
            throw new ReglaNegocioException("No hay un creador registrado para el tipo " + tipoPregunta + ".");
        }
        return creador;
    }
}
