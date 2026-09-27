package co.edu.unicauca.presentation;

import co.edu.unicauca.domain.Question;
import co.edu.unicauca.domain.QuestionService;
import co.edu.unicauca.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class GUIAssignReviewers extends JFrame {

    private final QuestionService questionService;
    private final List<User> revisoresDisponibles;
    private final Runnable onBack;

    private JComboBox<Question> cbPendientes;
    private JList<User> listRevisores;
    private JLabel lblAsignadosActuales;

    public GUIAssignReviewers(QuestionService questionService, List<User> revisoresDisponibles, Runnable onBack) {
        this.questionService = questionService;
        this.revisoresDisponibles = revisoresDisponibles;
        this.onBack = onBack;
        initComponents();
        cargarPendientes();
    }

    private void initComponents() {
        setTitle("Asignar Revisores - Banco Saber PRO");
        setIconImage(IconUtil.icon("reviewers", 32).getImage());
        setSize(560, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel pnlPendientes = new JPanel(new BorderLayout(8, 8));
        pnlPendientes.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Pregunta pendiente de revisión",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));
        cbPendientes = new JComboBox<>();
        cbPendientes.addActionListener(e -> actualizarAsignadosActuales());
        pnlPendientes.add(cbPendientes, BorderLayout.CENTER);
        lblAsignadosActuales = new JLabel("Revisores asignados actualmente: -");
        pnlPendientes.add(lblAsignadosActuales, BorderLayout.SOUTH);
        main.add(pnlPendientes);
        main.add(Box.createVerticalStrut(15));

        JPanel pnlRevisores = new JPanel(new BorderLayout(8, 8));
        pnlRevisores.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Seleccionar revisor(es) (Ctrl/Shift + clic para varios)",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));
        listRevisores = new JList<>(revisoresDisponibles.toArray(new User[0]));
        listRevisores.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listRevisores.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.getFullName() + " (" + value.getLogin() + ")"
                    + (value.getEmail() != null && !value.getEmail().isBlank() ? " - " + value.getEmail() : " - sin correo"));
            label.setOpaque(true);
            label.setBackground(isSelected ? new Color(37, 99, 235) : Color.WHITE);
            label.setForeground(isSelected ? Color.WHITE : Color.BLACK);
            label.setBorder(new EmptyBorder(4, 6, 4, 6));
            return label;
        });
        pnlRevisores.add(new JScrollPane(listRevisores), BorderLayout.CENTER);
        main.add(pnlRevisores);
        main.add(Box.createVerticalStrut(15));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnVolver = new JButton("Volver al tablero", IconUtil.icon("home", 16));
        btnVolver.addActionListener(e -> volver());
        JButton btnAsignar = new JButton("Asignar revisor(es)", IconUtil.icon("reviewers", 16));
        btnAsignar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAsignar.setBackground(new Color(37, 99, 235));
        btnAsignar.setForeground(Color.WHITE);
        btnAsignar.addActionListener(e -> asignar());
        botones.add(btnVolver);
        botones.add(btnAsignar);
        main.add(botones);

        add(main, BorderLayout.CENTER);
    }

    private void cargarPendientes() {
        cbPendientes.removeAllItems();
        for (Question q : questionService.getQuestionsPendingReview()) {
            cbPendientes.addItem(q);
        }
        actualizarAsignadosActuales();
    }

    private void actualizarAsignadosActuales() {
        Question q = (Question) cbPendientes.getSelectedItem();
        if (q == null) {
            lblAsignadosActuales.setText("Revisores asignados actualmente: -");
            return;
        }
        List<String> asignados = q.getRevisoresAsignados();
        lblAsignadosActuales.setText("Revisores asignados actualmente: "
                + (asignados == null || asignados.isEmpty() ? "ninguno" : String.join(", ", asignados)));
    }

    private void asignar() {
        Question q = (Question) cbPendientes.getSelectedItem();
        if (q == null) {
            JOptionPane.showMessageDialog(this, "No hay preguntas pendientes de revisión.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<User> seleccionados = listRevisores.getSelectedValuesList();
        if (seleccionados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione al menos un revisor.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean ok = questionService.assignReviewers(q.getId(), new ArrayList<>(seleccionados));
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Revisor(es) asignado(s) a la pregunta [" + q.getId() + "].\n"
                                + "Se envió (o registró) la notificación por correo a cada revisor.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarPendientes();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo asignar los revisores.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo asignar", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void volver() {
        dispose();
        if (onBack != null) {
            onBack.run();
        }
    }
}
