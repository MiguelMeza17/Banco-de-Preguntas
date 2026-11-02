package co.unicauca.bancopreguntas.preguntas.domain;

import co.unicauca.bancopreguntas.preguntas.PreguntasDePrueba;
import co.unicauca.bancopreguntas.preguntas.domain.estado.CodigoEstado;
import co.unicauca.bancopreguntas.preguntas.domain.fabrica.CreadorPreguntaSeleccionMultiple;
import co.unicauca.bancopreguntas.preguntas.domain.fabrica.FabricaPreguntas;
import co.unicauca.bancopreguntas.preguntas.domain.fabrica.PreguntaInvalidaException;
import co.unicauca.bancopreguntas.preguntas.web.adaptador.PreguntaRequestAdapter;
import co.unicauca.bancopreguntas.preguntas.web.dto.PreguntaRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Factory Method + Adapter + validacion estructural (HU03). */
class CreacionPreguntaTest {

    private final FabricaPreguntas fabrica = new FabricaPreguntas(List.of(new CreadorPreguntaSeleccionMultiple()));

    private Pregunta crear(PreguntaRequest request) {
        return fabrica.crear(new PreguntaRequestAdapter(request), request.usuarioId());
    }

    private PreguntaRequest conOpciones(String a, String b, String c, String d, String clave) {
        PreguntaRequest v = PreguntasDePrueba.valida();
        return new PreguntaRequest(v.usuarioId(), v.tipo(), v.titulo(), v.contexto(), v.enunciado(),
                a, b, c, d, clave, v.justificacion(), v.bibliografia(), v.competencia(), v.tema(),
                v.subtema(), v.dificultad());
    }

    private List<String> errores(PreguntaRequest request) {
        try {
            crear(request);
            return List.of();
        } catch (PreguntaInvalidaException e) {
            return e.getErrores();
        }
    }

    @Test
    void elAdaptadorTraduceElDtoYLaFabricaCreaLaPreguntaEnBorrador() {
        Pregunta p = crear(PreguntasDePrueba.valida());

        assertThat(p.getTipo()).isEqualTo(TipoPregunta.SELECCION_MULTIPLE_UNICA_RESPUESTA);
        assertThat(p.getEstado()).isEqualTo(CodigoEstado.BORRADOR);
        assertThat(p.getOpciones().comoLista()).containsExactly("2019", "2020", "2021", "2022");
        assertThat(p.getRespuestaCorrecta()).isEqualTo("C");
        assertThat(p.getDificultad()).isEqualTo(Dificultad.MEDIA);
        assertThat(p.getAutorId()).isEqualTo(PreguntasDePrueba.AUTOR);
    }

    @Test
    void sinContextoNiEnunciadoReportaTodosLosErrores() {
        PreguntaRequest v = PreguntasDePrueba.valida();
        PreguntaRequest sinDatos = new PreguntaRequest(v.usuarioId(), null, v.titulo(), " ", null,
                v.opcionA(), v.opcionB(), v.opcionC(), v.opcionD(), "A", v.justificacion(), v.bibliografia(),
                v.competencia(), v.tema(), v.subtema(), v.dificultad());

        assertThat(errores(sinDatos))
                .anyMatch(e -> e.contains("RF-08"))
                .anyMatch(e -> e.contains("RF-09"));
    }

    @Test
    void exigeCuatroOpcionesNoVacias() {
        assertThat(errores(conOpciones("2019", "", "2021", "2022", "A")))
                .anyMatch(e -> e.contains("opcion B") && e.contains("RF-10"));
    }

    @Test
    void exigeUnaUnicaRespuestaCorrectaValida() {
        assertThat(errores(conOpciones("2019", "2020", "2021", "2022", "E"))).anyMatch(e -> e.contains("RF-11"));
        assertThat(errores(conOpciones("2019", "2020", "2021", "2022", null))).anyMatch(e -> e.contains("RF-11"));
    }

    @Test
    void prohibeTodasYNingunaDeLasAnterioresSinImportarTildesNiMayusculas() {
        assertThat(errores(conOpciones("2019", "2020", "Todas las anteriores", "Ningúna  de las ANTERIORES", "A")))
                .filteredOn(e -> e.contains("RF-12"))
                .hasSize(2);
    }

    @Test
    void validaLongitudDeLasOpciones() {
        assertThat(errores(conOpciones("x", "2020", "2021", "2022", "A"))).anyMatch(e -> e.contains("RF-13"));
    }

    @Test
    void noPermiteOpcionesRepetidas() {
        assertThat(errores(conOpciones("2019", "2019 ", "2021", "2022", "A"))).anyMatch(e -> e.contains("duplicada"));
    }

    @Test
    void exigeUnaSolaPreguntaDirecta() {
        PreguntaRequest v = PreguntasDePrueba.valida();
        PreguntaRequest dos = new PreguntaRequest(v.usuarioId(), null, v.titulo(), v.contexto(),
                "¿Que ano crecio mas? ¿Y cual menos?", v.opcionA(), v.opcionB(), v.opcionC(), v.opcionD(), "A",
                v.justificacion(), v.bibliografia(), v.competencia(), v.tema(), v.subtema(), v.dificultad());

        assertThat(errores(dos)).anyMatch(e -> e.contains("unica pregunta directa"));
    }

    @Test
    void dificultadInvalidaSeReporta() {
        PreguntaRequest v = PreguntasDePrueba.conTemaYCompetencia("Estadistica", "RC", "MUY_ALTA", "t");

        assertThat(errores(v)).anyMatch(e -> e.contains("dificultad"));
    }

    @Test
    void tipoDesconocidoNoSeAcepta() {
        PreguntaRequest v = PreguntasDePrueba.valida();
        PreguntaRequest rara = new PreguntaRequest(v.usuarioId(), "ENSAYO", v.titulo(), v.contexto(), v.enunciado(),
                v.opcionA(), v.opcionB(), v.opcionC(), v.opcionD(), "A", v.justificacion(), v.bibliografia(),
                v.competencia(), v.tema(), v.subtema(), v.dificultad());

        assertThatThrownBy(() -> crear(rara))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no soportado");
    }
}
