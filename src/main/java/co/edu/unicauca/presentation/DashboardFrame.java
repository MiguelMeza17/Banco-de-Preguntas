package co.edu.unicauca.presentation;

import co.edu.unicauca.access.QuestionImplRepository;
import co.edu.unicauca.access.QuestionSqliteRepository;
import co.edu.unicauca.access.SmtpEmailNotifier;
import co.edu.unicauca.domain.QuestionRepository;
import co.edu.unicauca.domain.QuestionService;
import co.edu.unicauca.domain.QuestionStructuralValidator;
import co.edu.unicauca.domain.Role;
import co.edu.unicauca.domain.User;
import co.edu.unicauca.domain.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class DashboardFrame extends JFrame {

    private static QuestionService sharedQuestionService;

    private final User user;
    private final UserService userService;

    public DashboardFrame(User user, UserService userService) {
        this.user = user;
        this.userService = userService;

        setTitle("Tablero - " + user.getRole().getDisplayName());
        setIconImage(IconUtil.icon("home", 32).getImage());
        setSize(480, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        Role role = user.getRole();
        JLabel lblRoleIcon = new JLabel(IconUtil.icon(roleIconName(role), 48));
        lblRoleIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(lblRoleIcon);
        header.add(Box.createVerticalStrut(8));

        JLabel welcomeLabel = new JLabel("Bienvenido, " + user.getFullName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(welcomeLabel);

        JLabel roleLabel = new JLabel("Rol: " + role.getDisplayName(), SwingConstants.CENTER);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(Color.GRAY);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(roleLabel);

        panel.add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalStrut(10));

        if (role == Role.ADMINISTRADOR || role == Role.AUTOR_DE_PREGUNTAS || role == Role.REVISOR) {
            JButton btnBank = styledButton("Abrir Banco de Preguntas", new Color(37, 99, 235), "bank");
            btnBank.addActionListener(e -> openQuestionBank());
            centerPanel.add(btnBank);
            centerPanel.add(Box.createVerticalStrut(10));
        }

        if (role == Role.AUTOR_DE_PREGUNTAS) {
            JButton btnMisPreguntas = styledButton("Mis preguntas", new Color(124, 58, 237), "myquestions");
            btnMisPreguntas.addActionListener(e -> openMyQuestions());
            centerPanel.add(btnMisPreguntas);
            centerPanel.add(Box.createVerticalStrut(10));
        }

        if (role == Role.ADMINISTRADOR) {
            JButton btnAsignar = styledButton("Asignar revisores", new Color(217, 119, 6), "reviewers");
            btnAsignar.addActionListener(e -> openAssignReviewers());
            centerPanel.add(btnAsignar);
            centerPanel.add(Box.createVerticalStrut(10));

            JButton btnEmailConfig = styledButton("Configurar correo remitente", new Color(16, 129, 79), "email");
            btnEmailConfig.addActionListener(e -> openEmailConfig());
            centerPanel.add(btnEmailConfig);
            centerPanel.add(Box.createVerticalStrut(10));
        }

        if (role == Role.DOCENTE) {
            JLabel info = new JLabel("Vista Docente: reportes y seguimiento (pendiente).", SwingConstants.CENTER);
            info.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(info);
        }

        if (role == Role.ESTUDIANTE) {
            JLabel info = new JLabel("Vista Estudiante: simulacros (pendiente).", SwingConstants.CENTER);
            info.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(info);
        }

        panel.add(centerPanel, BorderLayout.CENTER);

        JButton btnLogout = new JButton("Cerrar sesión", IconUtil.icon("logout", 18));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> logout());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.add(btnLogout);
        panel.add(southPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private String roleIconName(Role role) {
        switch (role) {
            case ADMINISTRADOR: return "admin";
            case AUTOR_DE_PREGUNTAS: return "author";
            case REVISOR: return "search";
            case DOCENTE: return "teacher";
            case ESTUDIANTE: return "student";
            default: return "home";
        }
    }

    private JButton styledButton(String text, Color bg, String iconName) {
        JButton btn = new JButton(text, IconUtil.icon(iconName, 20));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setIconTextGap(10);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(320, 42));
        return btn;
    }

    private void logout() {
        dispose();
        LoginFrame loginFrame = new LoginFrame(userService);
        loginFrame.setVisible(true);
    }

    private void openQuestionBank() {
        setVisible(false);
        QuestionService qs = getSharedQuestionService();

        GUIQuestions guiQuestions = new GUIQuestions(qs, () -> setVisible(true));
        GUIObserver1 guiObserver1 = new GUIObserver1(qs);
        GUIObserver2 guiObserver2 = new GUIObserver2(qs);

        Point loc = guiQuestions.getLocation();
        guiObserver1.setLocation(loc.x + guiQuestions.getWidth() + 10, loc.y);
        guiObserver2.setLocation(loc.x + guiQuestions.getWidth() + 10, loc.y + guiObserver1.getHeight() + 10);

        guiQuestions.setVisible(true);
        guiObserver1.setVisible(true);
        guiObserver2.setVisible(true);
    }

    private void openMyQuestions() {
        setVisible(false);
        GUIMyQuestions myQuestions = new GUIMyQuestions(getSharedQuestionService(), user, () -> setVisible(true));
        myQuestions.setVisible(true);
    }

    private void openAssignReviewers() {
        setVisible(false);
        List<User> revisores = userService.getUsersByRole(Role.REVISOR);
        GUIAssignReviewers assignFrame = new GUIAssignReviewers(getSharedQuestionService(), revisores, () -> setVisible(true));
        assignFrame.setVisible(true);
    }

    private void openEmailConfig() {
        setVisible(false);
        GUIEmailConfig emailConfig = new GUIEmailConfig(() -> setVisible(true));
        emailConfig.setVisible(true);
    }

    /**
     * Servicio de preguntas compartido entre todas las ventanas, respaldado por SQLite
     * para que las preguntas persistan entre ejecuciones (HU01).
     */
    public static QuestionService getSharedQuestionService() {
        if (sharedQuestionService == null) {
            QuestionRepository repository = new QuestionSqliteRepository();
            sharedQuestionService = new QuestionService(
                    repository, new QuestionStructuralValidator(), new SmtpEmailNotifier());
        }
        return sharedQuestionService;
    }

    /**
     * Punto de entrada de depuración usado por {@code Main --questions}: abre el banco de
     * preguntas con datos de ejemplo en memoria, sin necesidad de iniciar sesión.
     */
    public static void launchQuestionBankSuite() {
        QuestionRepository questionRepository = new QuestionImplRepository();
        QuestionService questionService = new QuestionService(questionRepository);

        GUIQuestions guiQuestions = new GUIQuestions(questionService);
        GUIObserver1 guiObserver1 = new GUIObserver1(questionService);
        GUIObserver2 guiObserver2 = new GUIObserver2(questionService);

        Point loc = guiQuestions.getLocation();
        guiObserver1.setLocation(loc.x + guiQuestions.getWidth() + 10, loc.y);
        guiObserver2.setLocation(loc.x + guiQuestions.getWidth() + 10, loc.y + guiObserver1.getHeight() + 10);

        guiQuestions.setVisible(true);
        guiObserver1.setVisible(true);
        guiObserver2.setVisible(true);
    }
}
