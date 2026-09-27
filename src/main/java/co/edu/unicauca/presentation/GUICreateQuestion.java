package co.edu.unicauca.presentation;

import co.edu.unicauca.domain.Question;
import co.edu.unicauca.domain.QuestionDistractors;
import co.edu.unicauca.domain.QuestionService;
import co.edu.unicauca.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.UUID;

/**
 * Formulario de creación de preguntas de selección múltiple siguiendo el Diseño
 * Centrado en Evidencia (HU01): contexto, pregunta directa, 4 distractores, respuesta
 * correcta, justificación, bibliografía, competencia, tema, subtema y nivel de dificultad.
 * La validación estructural se aplica al grabar (QuestionService.createQuestion).
 */
public class GUICreateQuestion extends JFrame {

    private final QuestionService questionService;
    private final User autor;
    private final Runnable onBack;

    private JTextField txtNombre;
    private JTextArea txtContexto;
    private JTextArea txtPregunta;
    private JTextField txtOpcionA, txtOpcionB, txtOpcionC, txtOpcionD;
    private JRadioButton rbA, rbB, rbC, rbD;
    private JTextArea txtJustificacion;
    private JTextArea txtBibliografia;
    private JTextField txtCompetencia;
    private JTextField txtTema;
    private JTextField txtSubtema;
    private JComboBox<String> cbDificultad;

    public GUICreateQuestion(QuestionService questionService, User autor, Runnable onBack) {
        this.questionService = questionService;
        this.autor = autor;
        this.onBack = onBack;
        initComponents();
    }

    private void initComponents() {
        setTitle("Nueva Pregunta - Banco Saber PRO");
        setIconImage(IconUtil.icon("add", 32).getImage());
        setSize(620, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Título:"), gbc);
        txtNombre = new JTextField();
        gbc.gridx = 1; form.add(txtNombre, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Contexto:"), gbc);
        txtContexto = new JTextArea(3, 25);
        txtContexto.setLineWrap(true);
        txtContexto.setWrapStyleWord(true);
        gbc.gridx = 1; form.add(new JScrollPane(txtContexto), gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Pregunta directa:"), gbc);
        txtPregunta = new JTextArea(3, 25);
        txtPregunta.setLineWrap(true);
        txtPregunta.setWrapStyleWord(true);
        gbc.gridx = 1; form.add(new JScrollPane(txtPregunta), gbc);

        ButtonGroup correctGroup = new ButtonGroup();

        y++;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Opción A:"), gbc);
        txtOpcionA = new JTextField();
        rbA = new JRadioButton("Correcta");
        correctGroup.add(rbA);
        gbc.gridx = 1; form.add(opcionPanel(txtOpcionA, rbA), gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Opción B:"), gbc);
        txtOpcionB = new JTextField();
        rbB = new JRadioButton("Correcta");
        correctGroup.add(rbB);
        gbc.gridx = 1; form.add(opcionPanel(txtOpcionB, rbB), gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Opción C:"), gbc);
        txtOpcionC = new JTextField();
        rbC = new JRadioButton("Correcta");
        correctGroup.add(rbC);
        gbc.gridx = 1; form.add(opcionPanel(txtOpcionC, rbC), gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Opción D:"), gbc);
        txtOpcionD = new JTextField();
        rbD = new JRadioButton("Correcta");
        correctGroup.add(rbD);
        gbc.gridx = 1; form.add(opcionPanel(txtOpcionD, rbD), gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Justificación:"), gbc);
        txtJustificacion = new JTextArea(3, 25);
        txtJustificacion.setLineWrap(true);
        txtJustificacion.setWrapStyleWord(true);
        gbc.gridx = 1; form.add(new JScrollPane(txtJustificacion), gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Bibliografía:"), gbc);
        txtBibliografia = new JTextArea(2, 25);
        txtBibliografia.setLineWrap(true);
        txtBibliografia.setWrapStyleWord(true);
        gbc.gridx = 1; form.add(new JScrollPane(txtBibliografia), gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Competencia:"), gbc);
        txtCompetencia = new JTextField();
        gbc.gridx = 1; form.add(txtCompetencia, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Tema:"), gbc);
        txtTema = new JTextField();
        gbc.gridx = 1; form.add(txtTema, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Subtema:"), gbc);
        txtSubtema = new JTextField();
        gbc.gridx = 1; form.add(txtSubtema, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Nivel de dificultad:"), gbc);
        cbDificultad = new JComboBox<>(new String[]{"Fácil", "Media", "Difícil"});
        gbc.gridx = 1; form.add(cbDificultad, gbc);

        y++;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnVolver = new JButton("Volver", IconUtil.icon("home", 16));
        btnVolver.addActionListener(e -> volver());
        JButton btnGuardar = new JButton("Guardar pregunta", IconUtil.icon("add", 16));
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGuardar.setBackground(new Color(40, 167, 69));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> onGuardar());
        buttons.add(btnVolver);
        buttons.add(btnGuardar);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        form.add(buttons, gbc);

        add(new JScrollPane(form), BorderLayout.CENTER);
    }

    private JPanel opcionPanel(JTextField field, JRadioButton radio) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.add(field, BorderLayout.CENTER);
        panel.add(radio, BorderLayout.EAST);
        return panel;
    }

    private void onGuardar() {
        String correcta = null;
        if (rbA.isSelected()) correcta = txtOpcionA.getText();
        else if (rbB.isSelected()) correcta = txtOpcionB.getText();
        else if (rbC.isSelected()) correcta = txtOpcionC.getText();
        else if (rbD.isSelected()) correcta = txtOpcionD.getText();

        Question question = new Question.Builder()
                .id(UUID.randomUUID().toString().substring(0, 8))
                .nombre(txtNombre.getText())
                .contexto(txtContexto.getText())
                .pregunta(txtPregunta.getText())
                .distractors(new QuestionDistractors(
                        txtOpcionA.getText(), txtOpcionB.getText(), txtOpcionC.getText(), txtOpcionD.getText()))
                .respuestaCorrecta(correcta)
                .justificacion(txtJustificacion.getText())
                .bibliografia(txtBibliografia.getText())
                .competencia(txtCompetencia.getText())
                .tema(txtTema.getText())
                .subtema(txtSubtema.getText())
                .dificultad((String) cbDificultad.getSelectedItem())
                .estado(Question.STATE_BORRADOR)
                .autorLogin(autor.getLogin())
                .build();

        try {
            boolean saved = questionService.createQuestion(question);
            if (saved) {
                JOptionPane.showMessageDialog(this,
                        "Pregunta guardada en estado Borrador.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                volver();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar la pregunta.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación estructural", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void volver() {
        dispose();
        if (onBack != null) {
            onBack.run();
        }
    }
}
