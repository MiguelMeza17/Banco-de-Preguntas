package co.edu.unicauca.domain;

import co.edu.unicauca.access.QuestionImplRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de las funcionalidades agregadas para las HU01-HU04: creación con
 * validación estructural, envío a revisión, listado de "mis preguntas" con filtros
 * y asignación de revisores con notificación por correo.
 */
class QuestionServiceExtendedTest {

    private QuestionRepository repository;
    private RecordingEmailNotifier emailNotifier;
    private QuestionService service;

    @BeforeEach
    void setUp() {
        repository = new QuestionImplRepository();
        emailNotifier = new RecordingEmailNotifier();
        service = new QuestionService(repository, new QuestionStructuralValidator(), emailNotifier);
    }

    private Question.Builder validQuestion(String id, String autorLogin) {
        return new Question.Builder()
                .id(id)
                .nombre("Título " + id)
                .contexto("Contexto suficientemente descriptivo.")
                .pregunta("¿Pregunta directa?")
                .distractors(new QuestionDistractors("A", "B", "C", "D"))
                .respuestaCorrecta("A")
                .justificacion("Justificación con evidencia.")
                .bibliografia("Autor (2024). Libro.")
                .competencia("Interpretativa")
                .tema("Tema")
                .subtema("Subtema")
                .dificultad("Media")
                .autorLogin(autorLogin);
    }

    @Test
    @DisplayName("createQuestion debe guardar una pregunta válida en estado Borrador")
    void testCreateQuestionValid() {
        Question q = validQuestion("Q-100", "autor1").build();

        assertTrue(service.createQuestion(q));
        Question saved = service.getQuestionById("Q-100");
        assertNotNull(saved);
        assertEquals(Question.STATE_BORRADOR, saved.getEstado());
        assertEquals("autor1", saved.getAutorLogin());
    }

    @Test
    @DisplayName("createQuestion debe rechazar una pregunta con campos DCE incompletos")
    void testCreateQuestionInvalid() {
        Question incomplete = new Question.Builder().id("Q-101").nombre("Solo título").build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createQuestion(incomplete));
        assertTrue(ex.getMessage().toLowerCase().contains("contexto"));
    }

    @Test
    @DisplayName("submitForReview debe pasar una pregunta propia de Borrador a Pendiente de revisión")
    void testSubmitForReviewSuccess() {
        service.createQuestion(validQuestion("Q-102", "autor1").build());

        assertTrue(service.submitForReview("Q-102", "autor1"));
        assertEquals(Question.STATE_PENDIENTE_REVISION, service.getQuestionById("Q-102").getEstado());
    }

    @Test
    @DisplayName("submitForReview debe rechazar si el usuario no es el autor de la pregunta")
    void testSubmitForReviewWrongAuthor() {
        service.createQuestion(validQuestion("Q-103", "autor1").build());

        assertThrows(IllegalStateException.class, () -> service.submitForReview("Q-103", "otroAutor"));
        assertEquals(Question.STATE_BORRADOR, service.getQuestionById("Q-103").getEstado());
    }

    @Test
    @DisplayName("submitForReview debe rechazar si la pregunta no está en Borrador")
    void testSubmitForReviewWrongState() {
        service.createQuestion(validQuestion("Q-104", "autor1").build());
        service.submitForReview("Q-104", "autor1");

        assertThrows(IllegalStateException.class, () -> service.submitForReview("Q-104", "autor1"));
    }

    @Test
    @DisplayName("searchMyQuestions debe filtrar por autor, estado y texto libre")
    void testSearchMyQuestions() {
        service.createQuestion(validQuestion("Q-105", "autor1").tema("Redes").build());
        service.createQuestion(validQuestion("Q-106", "autor1").tema("Bases de datos").build());
        service.createQuestion(validQuestion("Q-107", "autor2").tema("Redes").build());

        List<Question> deAutor1 = service.searchMyQuestions("autor1", "Todos", "");
        assertEquals(2, deAutor1.size());

        List<Question> filtradoPorTexto = service.searchMyQuestions("autor1", "Todos", "redes");
        assertEquals(1, filtradoPorTexto.size());
        assertEquals("Q-105", filtradoPorTexto.get(0).getId());

        List<Question> filtradoPorEstado = service.searchMyQuestions("autor1", Question.STATE_PENDIENTE_REVISION, "");
        assertTrue(filtradoPorEstado.isEmpty());
    }

    @Test
    @DisplayName("paginate debe recortar la lista según página y tamaño")
    void testPaginate() {
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7);

        assertEquals(List.of(1, 2, 3), QuestionService.paginate(nums, 1, 3));
        assertEquals(List.of(4, 5, 6), QuestionService.paginate(nums, 2, 3));
        assertEquals(List.of(7), QuestionService.paginate(nums, 3, 3));
        assertTrue(QuestionService.paginate(nums, 4, 3).isEmpty());
    }

    @Test
    @DisplayName("assignReviewers debe asignar revisores y notificar por correo cuando la pregunta está Pendiente de revisión")
    void testAssignReviewersSuccess() {
        service.createQuestion(validQuestion("Q-108", "autor1").build());
        service.submitForReview("Q-108", "autor1");

        User revisor = new User("rev1", "Revisor Uno", "rev1@test.com", Role.REVISOR, UserStatus.ACTIVO, "hash");

        assertTrue(service.assignReviewers("Q-108", List.of(revisor)));

        Question updated = service.getQuestionById("Q-108");
        assertEquals(List.of("rev1"), updated.getRevisoresAsignados());
        assertEquals(1, emailNotifier.sentTo.size());
        assertEquals("rev1@test.com", emailNotifier.sentTo.get(0).getEmail());
    }

    @Test
    @DisplayName("assignReviewers debe rechazar si la pregunta no está Pendiente de revisión")
    void testAssignReviewersWrongState() {
        service.createQuestion(validQuestion("Q-109", "autor1").build());
        User revisor = new User("rev1", "Revisor Uno", "rev1@test.com", Role.REVISOR, UserStatus.ACTIVO, "hash");

        assertThrows(IllegalStateException.class, () -> service.assignReviewers("Q-109", List.of(revisor)));
        assertTrue(emailNotifier.sentTo.isEmpty());
    }

    @Test
    @DisplayName("assignReviewers debe rechazar si no se selecciona ningún revisor")
    void testAssignReviewersNoReviewers() {
        service.createQuestion(validQuestion("Q-110", "autor1").build());
        service.submitForReview("Q-110", "autor1");

        assertThrows(IllegalArgumentException.class, () -> service.assignReviewers("Q-110", new ArrayList<>()));
    }

    private static class RecordingEmailNotifier implements IEmailNotifier {
        final List<User> sentTo = new ArrayList<>();

        @Override
        public void sendReviewAssignmentEmail(User reviewer, Question question) {
            sentTo.add(reviewer);
        }
    }
}
