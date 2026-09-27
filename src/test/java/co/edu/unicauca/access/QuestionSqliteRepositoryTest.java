package co.edu.unicauca.access;

import co.edu.unicauca.domain.Question;
import co.edu.unicauca.domain.QuestionDistractors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionSqliteRepositoryTest {

    private QuestionSqliteRepository repository;

    @BeforeEach
    void setUp() {
        // Usa base de datos SQLite en memoria para no afectar users.db
        repository = new QuestionSqliteRepository("jdbc:sqlite::memory:");
    }

    private Question fullQuestion(String id, String autor, String estado) {
        return new Question.Builder()
                .id(id)
                .nombre("Título " + id)
                .contexto("Contexto de prueba")
                .pregunta("¿Pregunta directa?")
                .distractors(new QuestionDistractors("A", "B", "C", "D"))
                .respuestaCorrecta("A")
                .justificacion("Justificación")
                .bibliografia("Bibliografía")
                .competencia("Competencia")
                .tema("Tema")
                .subtema("Subtema")
                .dificultad("Media")
                .estado(estado)
                .autorLogin(autor)
                .build();
    }

    @Test
    @DisplayName("Debe guardar y recuperar una pregunta completa (DCE) por id")
    void testSaveAndFindById() {
        assertTrue(repository.save(fullQuestion("P-1", "autor1", Question.STATE_BORRADOR)));

        Question found = repository.findById("P-1");
        assertNotNull(found);
        assertEquals("Contexto de prueba", found.getContexto());
        assertEquals("Justificación", found.getJustificacion());
        assertEquals("autor1", found.getAutorLogin());
        assertEquals("A", found.getDistractors().getOptionA());
    }

    @Test
    @DisplayName("Debe listar preguntas por autor")
    void testFindByAutor() {
        repository.save(fullQuestion("P-2", "autor1", Question.STATE_BORRADOR));
        repository.save(fullQuestion("P-3", "autor2", Question.STATE_BORRADOR));

        List<Question> deAutor1 = repository.findByAutor("autor1");
        assertEquals(1, deAutor1.size());
        assertEquals("P-2", deAutor1.get(0).getId());
    }

    @Test
    @DisplayName("Debe listar preguntas por estado")
    void testFindByEstado() {
        repository.save(fullQuestion("P-4", "autor1", Question.STATE_PENDIENTE_REVISION));
        repository.save(fullQuestion("P-5", "autor1", Question.STATE_BORRADOR));

        List<Question> pendientes = repository.findByEstado(Question.STATE_PENDIENTE_REVISION);
        assertEquals(1, pendientes.size());
        assertEquals("P-4", pendientes.get(0).getId());
    }

    @Test
    @DisplayName("Debe actualizar el estado de una pregunta")
    void testUpdateState() {
        repository.save(fullQuestion("P-6", "autor1", Question.STATE_BORRADOR));

        assertTrue(repository.updateState("P-6", Question.STATE_PENDIENTE_REVISION));
        assertEquals(Question.STATE_PENDIENTE_REVISION, repository.findById("P-6").getEstado());
    }

    @Test
    @DisplayName("Debe asignar y recuperar revisores de una pregunta")
    void testAssignReviewers() {
        repository.save(fullQuestion("P-7", "autor1", Question.STATE_PENDIENTE_REVISION));

        assertTrue(repository.assignReviewers("P-7", List.of("rev1", "rev2")));

        Question q = repository.findById("P-7");
        assertEquals(2, q.getRevisoresAsignados().size());
        assertTrue(q.getRevisoresAsignados().containsAll(List.of("rev1", "rev2")));
    }
}
