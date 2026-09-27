package co.edu.unicauca.domain;

public interface IEmailNotifier {
    void sendReviewAssignmentEmail(User reviewer, Question question) throws Exception;
}
