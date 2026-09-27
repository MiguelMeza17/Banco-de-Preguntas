package co.edu.unicauca.domain;

import java.util.ArrayList;
import java.util.List;

public interface QuestionRepository {
    List<Question> findAll();
    Question findById(String id);
    boolean updateState(String id, String newState);
    boolean save(Question question);


    default List<Question> findByAutor(String autorLogin) {
        List<Question> result = new ArrayList<>();
        if (autorLogin == null) {
            return result;
        }
        for (Question q : findAll()) {
            if (autorLogin.equals(q.getAutorLogin())) {
                result.add(q);
            }
        }
        return result;
    }

    default List<Question> findByEstado(String estado) {
        List<Question> result = new ArrayList<>();
        if (estado == null) {
            return result;
        }
        for (Question q : findAll()) {
            if (estado.equals(q.getEstado())) {
                result.add(q);
            }
        }
        return result;
    }

    default boolean assignReviewers(String questionId, List<String> reviewerLogins) {
        Question q = findById(questionId);
        if (q == null) {
            return false;
        }
        q.setRevisoresAsignados(new ArrayList<>(reviewerLogins));
        return true;
    }
}
