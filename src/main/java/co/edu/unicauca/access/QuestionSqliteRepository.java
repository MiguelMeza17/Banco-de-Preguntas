package co.edu.unicauca.access;

import co.edu.unicauca.domain.Question;
import co.edu.unicauca.domain.QuestionDistractors;
import co.edu.unicauca.domain.QuestionRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuestionSqliteRepository implements QuestionRepository, AutoCloseable {

    private final String dbUrl;
    private Connection memoryConnection;

    public QuestionSqliteRepository() {
        this("jdbc:sqlite:users.db");
    }

    public QuestionSqliteRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        if (dbUrl.contains(":memory:")) {
            try {
                this.memoryConnection = DriverManager.getConnection(dbUrl);
            } catch (SQLException e) {
                System.err.println("Error creando conexión en memoria para SQLite: " + e.getMessage());
            }
        }
        initDatabase();
        seedSampleDataIfEmpty();
    }

    private Connection getConnection() throws SQLException {
        if (memoryConnection != null && !memoryConnection.isClosed()) {
            return memoryConnection;
        }
        return DriverManager.getConnection(dbUrl);
    }

    private void closeIfNew(Connection conn) {
        if (conn != memoryConnection && conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private void initDatabase() {
        String questionsSql = "CREATE TABLE IF NOT EXISTS questions (" +
                "id TEXT PRIMARY KEY, " +
                "nombre TEXT NOT NULL, " +
                "contexto TEXT, " +
                "pregunta TEXT NOT NULL, " +
                "opcion_a TEXT, " +
                "opcion_b TEXT, " +
                "opcion_c TEXT, " +
                "opcion_d TEXT, " +
                "respuesta_correcta TEXT, " +
                "justificacion TEXT, " +
                "bibliografia TEXT, " +
                "competencia TEXT, " +
                "tema TEXT, " +
                "subtema TEXT, " +
                "dificultad TEXT, " +
                "estado TEXT NOT NULL, " +
                "autor_login TEXT" +
                ");";

        String reviewersSql = "CREATE TABLE IF NOT EXISTS question_reviewers (" +
                "question_id TEXT NOT NULL, " +
                "reviewer_login TEXT NOT NULL, " +
                "PRIMARY KEY (question_id, reviewer_login)" +
                ");";

        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(questionsSql);
                stmt.execute(reviewersSql);
            }
        } catch (SQLException e) {
            System.err.println("Error inicializando la base de datos SQLite (preguntas): " + e.getMessage());
        } finally {
            closeIfNew(conn);
        }
    }

    private void seedSampleDataIfEmpty() {
        if (dbUrl.contains(":memory:")) 
        {
            return;
        }

        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM questions")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error verificando datos de muestra: " + e.getMessage());
            return;
        } finally {
            closeIfNew(conn);
        }

        save(new Question("P-001", "Pregunta sobre DDD",
                "¿Cuál es el objetivo principal de DDD?",
                new QuestionDistractors("Diseñar bases de datos", "Modelar el dominio del negocio",
                        "Eliminar UML", "Crear interfaces gráficas"),
                "B", Question.STATE_BORRADOR));

        save(new Question("P-002", "Arquitectura en capas",
                "¿Qué capa es responsable de abstraer el acceso a datos y persistencia?",
                new QuestionDistractors("Capa de Presentación", "Capa de Dominio",
                        "Capa de Acceso a Datos", "Capa Transversal"),
                "C", Question.STATE_BORRADOR));

        save(new Question("P-003", "Principios SOLID",
                "¿Qué establece el Principio de Inversión de Dependencias (DIP)?",
                new QuestionDistractors("Los módulos de alto nivel deben depender de abstracciones",
                        "Las clases deben ser abiertas para modificación",
                        "Usar solo una interfaz por aplicación", "Evitar el patrón de diseño Observer"),
                "A", Question.STATE_PENDIENTE_REVISION));

        save(new Question("P-004", "Patrón Observer",
                "¿Cuál es el propósito fundamental del patrón de diseño Observer?",
                new QuestionDistractors("Instanciar objetos de manera dinámica",
                        "Definir una dependencia de uno a muchos entre objetos",
                        "Encapsular algoritmos en clases independientes",
                        "Convertir la interfaz de una clase en otra"),
                "B", Question.STATE_PENDIENTE_REVISION));

        save(new Question("P-005", "Patrones Creacionales",
                "¿Cuál de los siguientes patrones pertenece a la categoría Creacional?",
                new QuestionDistractors("Adapter", "Singleton", "Observer", "Strategy"),
                "B", Question.STATE_ELIMINADA));
    }

    @Override
    public boolean save(Question question) {
        if (question == null || question.getId() == null) {
            return false;
        }

        String sql = "INSERT INTO questions(id, nombre, contexto, pregunta, opcion_a, opcion_b, opcion_c, " +
                "opcion_d, respuesta_correcta, justificacion, bibliografia, competencia, tema, subtema, " +
                "dificultad, estado, autor_login) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        Connection conn = null;
        try {
            conn = getConnection();
            QuestionDistractors d = question.getDistractors();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, question.getId());
                pstmt.setString(2, question.getNombre());
                pstmt.setString(3, question.getContexto());
                pstmt.setString(4, question.getPregunta());
                pstmt.setString(5, d != null ? d.getOptionA() : null);
                pstmt.setString(6, d != null ? d.getOptionB() : null);
                pstmt.setString(7, d != null ? d.getOptionC() : null);
                pstmt.setString(8, d != null ? d.getOptionD() : null);
                pstmt.setString(9, question.getRespuestaCorrecta());
                pstmt.setString(10, question.getJustificacion());
                pstmt.setString(11, question.getBibliografia());
                pstmt.setString(12, question.getCompetencia());
                pstmt.setString(13, question.getTema());
                pstmt.setString(14, question.getSubtema());
                pstmt.setString(15, question.getDificultad());
                pstmt.setString(16, question.getEstado() != null ? question.getEstado() : Question.STATE_BORRADOR);
                pstmt.setString(17, question.getAutorLogin());

                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar la pregunta en SQLite: " + e.getMessage());
            return false;
        } finally {
            closeIfNew(conn);
        }
    }

    @Override
    public Question findById(String id) {
        if (id == null) {
            return null;
        }
        String sql = "SELECT * FROM questions WHERE id = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Question q = mapResultSetToQuestion(rs);
                        q.setRevisoresAsignados(loadReviewers(conn, id));
                        return q;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar pregunta por id: " + e.getMessage());
        } finally {
            closeIfNew(conn);
        }
        return null;
    }

    @Override
    public List<Question> findAll() {
        return queryQuestions("SELECT * FROM questions", null);
    }

    @Override
    public List<Question> findByAutor(String autorLogin) {
        return queryQuestions("SELECT * FROM questions WHERE autor_login = ?", autorLogin);
    }

    @Override
    public List<Question> findByEstado(String estado) {
        return queryQuestions("SELECT * FROM questions WHERE estado = ?", estado);
    }

    private List<Question> queryQuestions(String sql, String param) {
        List<Question> result = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (param != null) {
                    pstmt.setString(1, param);
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Question q = mapResultSetToQuestion(rs);
                        q.setRevisoresAsignados(loadReviewers(conn, q.getId()));
                        result.add(q);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar preguntas: " + e.getMessage());
        } finally {
            closeIfNew(conn);
        }
        return result;
    }

    @Override
    public boolean updateState(String id, String newState) {
        String sql = "UPDATE questions SET estado = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newState);
                pstmt.setString(2, id);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar el estado de la pregunta: " + e.getMessage());
            return false;
        } finally {
            closeIfNew(conn);
        }
    }

    @Override
    public boolean assignReviewers(String questionId, List<String> reviewerLogins) {
        if (questionId == null || reviewerLogins == null) {
            return false;
        }

        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM question_reviewers WHERE question_id = ?")) {
                del.setString(1, questionId);
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO question_reviewers(question_id, reviewer_login) VALUES (?, ?)")) {
                for (String reviewerLogin : reviewerLogins) {
                    ins.setString(1, questionId);
                    ins.setString(2, reviewerLogin);
                    ins.addBatch();
                }
                ins.executeBatch();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error al asignar revisores: " + e.getMessage());
            return false;
        } finally {
            closeIfNew(conn);
        }
    }

    private List<String> loadReviewers(Connection conn, String questionId) throws SQLException {
        List<String> reviewers = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT reviewer_login FROM question_reviewers WHERE question_id = ?")) {
            pstmt.setString(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reviewers.add(rs.getString("reviewer_login"));
                }
            }
        }
        return reviewers;
    }

    private Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
        QuestionDistractors distractors = new QuestionDistractors(
                rs.getString("opcion_a"),
                rs.getString("opcion_b"),
                rs.getString("opcion_c"),
                rs.getString("opcion_d")
        );

        return new Question.Builder()
                .id(rs.getString("id"))
                .nombre(rs.getString("nombre"))
                .contexto(rs.getString("contexto"))
                .pregunta(rs.getString("pregunta"))
                .distractors(distractors)
                .respuestaCorrecta(rs.getString("respuesta_correcta"))
                .justificacion(rs.getString("justificacion"))
                .bibliografia(rs.getString("bibliografia"))
                .competencia(rs.getString("competencia"))
                .tema(rs.getString("tema"))
                .subtema(rs.getString("subtema"))
                .dificultad(rs.getString("dificultad"))
                .estado(rs.getString("estado"))
                .autorLogin(rs.getString("autor_login"))
                .build();
    }

    @Override
    public void close() {
        if (memoryConnection != null) {
            try {
                memoryConnection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
