package co.edu.unicauca.domain;

import co.edu.unicauca.infra.Subject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio del dominio para la gestión del banco de preguntas.
 * Extiende de Subject para notificar a los observadores cuando cambie el estado de alguna pregunta.
 */
public class QuestionService extends Subject {
    private final QuestionRepository repository;
    private final IQuestionValidator validator;
    private final IEmailNotifier emailNotifier;

    public QuestionService(QuestionRepository repository) {
        this(repository, new QuestionStructuralValidator(), null);
    }

    public QuestionService(QuestionRepository repository, IQuestionValidator validator, IEmailNotifier emailNotifier) {
        if (repository == null) {
            throw new IllegalArgumentException("El repositorio no puede ser nulo.");
        }
        this.repository = repository;
        this.validator = validator != null ? validator : new QuestionStructuralValidator();
        this.emailNotifier = emailNotifier;
    }

    public List<Question> getAllQuestions() {
        return repository.findAll();
    }

    public Question getQuestionById(String id) {
        return repository.findById(id);
    }

    /**
     * Actualiza el estado de una pregunta y notifica a todos los observadores registrados.
     */
    public boolean updateQuestionState(String id, String newState) {
        boolean updated = repository.updateState(id, newState);
        if (updated) {
            notifyAllObservers();
        }
        return updated;
    }

    /**
     * Guarda una nueva pregunta y notifica a los observadores.
     */
    public boolean saveQuestion(Question question) {
        boolean saved = repository.save(question);
        if (saved) {
            notifyAllObservers();
        }
        return saved;
    }

    /**
     * Crea una nueva pregunta aplicando la validación estructural del DCE (HU01).
     * Si la pregunta no trae estado, nace en Borrador.
     *
     * @throws IllegalArgumentException con el detalle de los campos inválidos.
     */
    public boolean createQuestion(Question question) {
        List<String> errors = validator.validate(question);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
        if (question.getEstado() == null || question.getEstado().isBlank()) {
            question.setEstado(Question.STATE_BORRADOR);
        }
        return saveQuestion(question);
    }

    /**
     * Envía una pregunta propia de Borrador a Pendiente de revisión (HU02).
     * Solo el autor de la pregunta puede hacerlo, y solo desde el estado Borrador.
     */
    public boolean submitForReview(String questionId, String autorLogin) {
        Question question = repository.findById(questionId);
        if (question == null) {
            throw new IllegalArgumentException("La pregunta no existe.");
        }
        if (autorLogin == null || !autorLogin.equals(question.getAutorLogin())) {
            throw new IllegalStateException("Solo el autor de la pregunta puede enviarla a revisión.");
        }
        if (!Question.STATE_BORRADOR.equals(question.getEstado())) {
            throw new IllegalStateException("Solo se puede enviar a revisión una pregunta en estado Borrador.");
        }
        return updateQuestionState(questionId, Question.STATE_PENDIENTE_REVISION);
    }

    /**
     * Preguntas creadas por un autor (HU03 - "Mis preguntas").
     */
    public List<Question> getQuestionsByAuthor(String autorLogin) {
        return repository.findByAutor(autorLogin);
    }

    /**
     * Búsqueda de "mis preguntas" con filtro opcional de estado y texto libre (HU03).
     *
     * @param estadoFiltro estado exacto a filtrar, o null/"Todos" para no filtrar.
     * @param textoFiltro  texto libre buscado en título, tema y enunciado (case-insensitive).
     */
    public List<Question> searchMyQuestions(String autorLogin, String estadoFiltro, String textoFiltro) {
        List<Question> result = new ArrayList<>();
        String texto = textoFiltro != null ? textoFiltro.trim().toLowerCase() : "";

        for (Question q : repository.findByAutor(autorLogin)) {
            if (estadoFiltro != null && !estadoFiltro.isBlank()
                    && !"Todos".equalsIgnoreCase(estadoFiltro) && !estadoFiltro.equals(q.getEstado())) {
                continue;
            }
            if (!texto.isEmpty() && !matchesText(q, texto)) {
                continue;
            }
            result.add(q);
        }
        return result;
    }

    private boolean matchesText(Question q, String texto) {
        return contains(q.getNombre(), texto) || contains(q.getTema(), texto) || contains(q.getPregunta(), texto);
    }

    private boolean contains(String field, String texto) {
        return field != null && field.toLowerCase().contains(texto);
    }

    /**
     * Recorta una lista a la página solicitada (1-based). Utilidad para paginar en las vistas (HU03).
     */
    public static <T> List<T> paginate(List<T> list, int page, int pageSize) {
        if (list.isEmpty() || pageSize <= 0) {
            return new ArrayList<>();
        }
        int from = Math.max(0, (page - 1) * pageSize);
        if (from >= list.size()) {
            return new ArrayList<>();
        }
        int to = Math.min(list.size(), from + pageSize);
        return new ArrayList<>(list.subList(from, to));
    }

    /**
     * Preguntas en estado "Pendiente de revisión" (HU04 - bandeja del administrador).
     */
    public List<Question> getQuestionsPendingReview() {
        return repository.findByEstado(Question.STATE_PENDIENTE_REVISION);
    }

    /**
     * Asigna uno o más revisores a una pregunta en estado "Pendiente de revisión" y
     * notifica por correo a cada uno (HU04). Los errores de envío de correo no
     * impiden la asignación; solo quedan registrados en consola.
     */
    public boolean assignReviewers(String questionId, List<User> reviewers) {
        Question question = repository.findById(questionId);
        if (question == null) {
            throw new IllegalArgumentException("La pregunta no existe.");
        }
        if (!Question.STATE_PENDIENTE_REVISION.equals(question.getEstado())) {
            throw new IllegalStateException("Solo se pueden asignar revisores a preguntas en estado Pendiente de revisión.");
        }
        if (reviewers == null || reviewers.isEmpty()) {
            throw new IllegalArgumentException("Debe asignar al menos un revisor.");
        }

        List<String> reviewerLogins = new ArrayList<>();
        for (User reviewer : reviewers) {
            reviewerLogins.add(reviewer.getLogin());
        }

        boolean assigned = repository.assignReviewers(questionId, reviewerLogins);
        if (assigned) {
            notifyReviewers(reviewers, question);
            notifyAllObservers();
        }
        return assigned;
    }

    private void notifyReviewers(List<User> reviewers, Question question) {
        if (emailNotifier == null) {
            return;
        }
        for (User reviewer : reviewers) {
            try {
                emailNotifier.sendReviewAssignmentEmail(reviewer, question);
            } catch (Exception e) {
                System.err.println("No se pudo notificar por correo a " + reviewer.getLogin() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene el conteo total de preguntas por cada estado.
     */
    public Map<String, Integer> getQuestionCountByState() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put(Question.STATE_BORRADOR, 0);
        counts.put(Question.STATE_PENDIENTE_REVISION, 0);
        counts.put(Question.STATE_ELIMINADA, 0);

        List<Question> questions = repository.findAll();
        for (Question q : questions) {
            String state = q.getEstado();
            if (counts.containsKey(state)) {
                counts.put(state, counts.get(state) + 1);
            } else {
                counts.put(state, 1);
            }
        }
        return counts;
    }

    /**
     * Obtiene el porcentaje de preguntas por cada estado.
     */
    public Map<String, Double> getQuestionPercentageByState() {
        Map<String, Integer> counts = getQuestionCountByState();
        Map<String, Double> percentages = new LinkedHashMap<>();
        int total = repository.findAll().size();

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            double pct = (total == 0) ? 0.0 : (entry.getValue() * 100.0) / total;
            percentages.put(entry.getKey(), Math.round(pct * 10.0) / 10.0);
        }
        return percentages;
    }

    public int getTotalQuestionsCount() {
        return repository.findAll().size();
    }
}
