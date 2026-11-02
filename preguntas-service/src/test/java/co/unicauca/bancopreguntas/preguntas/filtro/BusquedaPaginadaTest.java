package co.unicauca.bancopreguntas.preguntas.filtro;

import co.unicauca.bancopreguntas.preguntas.PreguntasDePrueba;
import co.unicauca.bancopreguntas.preguntas.domain.Dificultad;
import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import co.unicauca.bancopreguntas.preguntas.domain.estado.CodigoEstado;
import co.unicauca.bancopreguntas.preguntas.repository.PreguntaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Estrategias de filtrado contra una base H2 real: los filtros y la paginacion se resuelven en
 * la consulta SQL (HU-3).
 */
@DataJpaTest
@Import({CombinadorFiltros.class, FiltroPorAutor.class, FiltroPorEstado.class, FiltroPorTema.class,
        FiltroPorCompetencia.class, FiltroPorDificultad.class, FiltroPorTexto.class})
class BusquedaPaginadaTest {

    @Autowired
    private PreguntaRepository repository;

    @Autowired
    private CombinadorFiltros combinador;

    @BeforeEach
    void datos() {
        for (int i = 1; i <= 7; i++) {
            repository.save(PreguntasDePrueba.pregunta(PreguntasDePrueba.conTemaYCompetencia(
                    "Estadistica", "Razonamiento cuantitativo", "MEDIA", "Graficas " + i)));
        }
        repository.save(PreguntasDePrueba.pregunta(PreguntasDePrueba.conTemaYCompetencia(
                "Algebra", "Razonamiento cuantitativo", "ALTA", "Ecuaciones lineales")));
        repository.save(PreguntasDePrueba.pregunta(PreguntasDePrueba.conTemaYCompetencia(
                "Argumentacion", "Lectura critica", "BAJA", "Tesis del autor")));

        Pregunta enviada = PreguntasDePrueba.pregunta(PreguntasDePrueba.conTemaYCompetencia(
                "Algebra", "Razonamiento cuantitativo", "BAJA", "Funciones"));
        enviada.enviarARevision(PreguntasDePrueba.AUTOR);
        repository.save(enviada);
    }

    private Page<Pregunta> buscar(CriteriosBusqueda c, int pagina, int tamano) {
        return repository.findAll(combinador.combinar(c), PageRequest.of(pagina, tamano, Sort.by("id")));
    }

    private CriteriosBusqueda criterios(Long autor, CodigoEstado estado, String tema, String competencia,
                                        Dificultad dificultad, String texto) {
        return new CriteriosBusqueda(autor, estado, tema, competencia, dificultad, texto);
    }

    @Test
    void sinFiltrosDevuelveTodoPaginado() {
        Page<Pregunta> pagina = buscar(CriteriosBusqueda.vacios(), 0, 4);

        assertThat(pagina.getTotalElements()).isEqualTo(10);
        assertThat(pagina.getTotalPages()).isEqualTo(3);
        assertThat(pagina.getContent()).hasSize(4);
        assertThat(combinador.contarAplicables(CriteriosBusqueda.vacios())).isZero();
    }

    @Test
    void laUltimaPaginaTraeElResto() {
        assertThat(buscar(CriteriosBusqueda.vacios(), 2, 4).getContent()).hasSize(2);
        assertThat(buscar(CriteriosBusqueda.vacios(), 3, 4).getContent()).isEmpty();
    }

    @Test
    void filtraPorTemaSinImportarMayusculas() {
        assertThat(buscar(criterios(null, null, "ESTADISTICA", null, null, null), 0, 50).getTotalElements())
                .isEqualTo(7);
    }

    @Test
    void filtraPorCompetencia() {
        assertThat(buscar(criterios(null, null, null, "Lectura critica", null, null), 0, 50).getContent())
                .extracting(Pregunta::getTitulo).containsExactly("Tesis del autor");
    }

    @Test
    void filtraPorEstado() {
        assertThat(buscar(criterios(null, CodigoEstado.PENDIENTE_REVISION, null, null, null, null), 0, 50)
                .getContent()).extracting(Pregunta::getTitulo).containsExactly("Funciones");
    }

    @Test
    void combinaVariosFiltrosConY() {
        CriteriosBusqueda c = criterios(PreguntasDePrueba.AUTOR, CodigoEstado.BORRADOR, "algebra",
                "Razonamiento cuantitativo", Dificultad.ALTA, null);

        assertThat(combinador.contarAplicables(c)).isEqualTo(5);
        assertThat(buscar(c, 0, 50).getContent()).extracting(Pregunta::getTitulo)
                .containsExactly("Ecuaciones lineales");
    }

    @Test
    void textoLibreBuscaEnTituloEnunciadoYTema() {
        assertThat(buscar(criterios(null, null, null, null, null, "graficas 7"), 0, 50).getContent())
                .extracting(Pregunta::getTitulo).containsExactly("Graficas 7");
        assertThat(buscar(criterios(null, null, null, null, null, "argument"), 0, 50).getTotalElements())
                .isEqualTo(1);
    }

    @Test
    void otroAutorNoVeLasPreguntasAjenas() {
        assertThat(buscar(criterios(99L, null, null, null, null, null), 0, 50).getContent()).isEmpty();
    }

    @Test
    void losComodinesDelTextoSeTratanComoTextoLiteral() {
        assertThat(buscar(criterios(null, null, null, null, null, "%"), 0, 50).getContent()).isEmpty();
        assertThat(List.of(buscar(criterios(null, null, null, null, null, "_"), 0, 50).getTotalElements()))
                .containsExactly(0L);
    }
}
