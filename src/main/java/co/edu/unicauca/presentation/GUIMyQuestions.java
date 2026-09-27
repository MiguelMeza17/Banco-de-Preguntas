package co.edu.unicauca.presentation;

import co.edu.unicauca.domain.Question;
import co.edu.unicauca.domain.QuestionService;
import co.edu.unicauca.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * "Mis preguntas": listado de las preguntas creadas por el autor autenticado, con
 * filtros por estado y texto, paginación (HU03), colores por estado y la acción de
 * enviar una pregunta propia de Borrador a Pendiente de revisión (HU02).
 */
public class GUIMyQuestions extends JFrame {

    private static final int PAGE_SIZE = 5;

    private final QuestionService questionService;
    private final User autor;
    private final Runnable onBack;

    private JComboBox<String> cbEstado;
    private JTextField txtBuscar;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblPagina;
    private JButton btnAnterior, btnSiguiente, btnEnviarRevision;

    private List<Question> resultados;
    private int paginaActual = 1;

    public GUIMyQuestions(QuestionService questionService, User autor, Runnable onBack) {
        this.questionService = questionService;
        this.autor = autor;
        this.onBack = onBack;
        initComponents();
        buscar();
    }

    private void initComponents() {
        setTitle("Mis Preguntas - " + autor.getFullName());
        setIconImage(IconUtil.icon("myquestions", 32).getImage());
        setSize(760, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Filtros ---
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filtros.setBorder(new EmptyBorder(10, 10, 0, 10));

        filtros.add(new JLabel("Estado:"));
        cbEstado = new JComboBox<>(new String[]{
                "Todos", Question.STATE_BORRADOR, Question.STATE_PENDIENTE_REVISION, Question.STATE_ELIMINADA
        });
        filtros.add(cbEstado);

        filtros.add(new JLabel("Buscar:"));
        txtBuscar = new JTextField(15);
        filtros.add(txtBuscar);

        JButton btnBuscar = new JButton("Filtrar", IconUtil.icon("search", 16));
        btnBuscar.addActionListener(e -> { paginaActual = 1; buscar(); });
        filtros.add(btnBuscar);

        JButton btnNueva = new JButton("Nueva pregunta", IconUtil.icon("add", 16));
        btnNueva.setBackground(new Color(16, 129, 79));
        btnNueva.setForeground(Color.WHITE);
        btnNueva.addActionListener(e -> {
            GUICreateQuestion create = new GUICreateQuestion(questionService, autor, this::buscar);
            create.setVisible(true);
        });
        filtros.add(btnNueva);

        add(filtros, BorderLayout.NORTH);

        // --- Tabla ---
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Título", "Tema", "Competencia", "Dificultad", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(5).setCellRenderer(new EstadoCellRenderer());
        table.getSelectionModel().addListSelectionListener(e -> actualizarBotonEnviar());
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Pie: paginación + acciones ---
        JPanel pie = new JPanel(new BorderLayout());
        pie.setBorder(new EmptyBorder(8, 10, 10, 10));

        JPanel paginacion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAnterior = new JButton("« Anterior");
        btnAnterior.addActionListener(e -> { paginaActual--; renderPagina(); });
        lblPagina = new JLabel("Página 1 de 1");
        btnSiguiente = new JButton("Siguiente »");
        btnSiguiente.addActionListener(e -> { paginaActual++; renderPagina(); });
        paginacion.add(btnAnterior);
        paginacion.add(lblPagina);
        paginacion.add(btnSiguiente);
        pie.add(paginacion, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEnviarRevision = new JButton("Enviar a revisión", IconUtil.icon("email", 16));
        btnEnviarRevision.setBackground(new Color(217, 119, 6));
        btnEnviarRevision.setForeground(Color.WHITE);
        btnEnviarRevision.setEnabled(false);
        btnEnviarRevision.addActionListener(e -> enviarARevision());
        JButton btnVolver = new JButton("Volver al tablero", IconUtil.icon("home", 16));
        btnVolver.addActionListener(e -> volver());
        acciones.add(btnEnviarRevision);
        acciones.add(btnVolver);
        pie.add(acciones, BorderLayout.SOUTH);

        add(pie, BorderLayout.SOUTH);
    }

    private void buscar() {
        String estado = (String) cbEstado.getSelectedItem();
        resultados = questionService.searchMyQuestions(autor.getLogin(), estado, txtBuscar.getText());
        paginaActual = 1;
        renderPagina();
    }

    private void renderPagina() {
        int totalPaginas = Math.max(1, (int) Math.ceil(resultados.size() / (double) PAGE_SIZE));
        if (paginaActual < 1) paginaActual = 1;
        if (paginaActual > totalPaginas) paginaActual = totalPaginas;

        List<Question> pagina = QuestionService.paginate(resultados, paginaActual, PAGE_SIZE);

        tableModel.setRowCount(0);
        for (Question q : pagina) {
            tableModel.addRow(new Object[]{
                    q.getId(), q.getNombre(), q.getTema(), q.getCompetencia(), q.getDificultad(), q.getEstado()
            });
        }

        lblPagina.setText("Página " + paginaActual + " de " + totalPaginas + " (" + resultados.size() + " preguntas)");
        btnAnterior.setEnabled(paginaActual > 1);
        btnSiguiente.setEnabled(paginaActual < totalPaginas);
        actualizarBotonEnviar();
    }

    private void actualizarBotonEnviar() {
        int row = table.getSelectedRow();
        if (row < 0) {
            btnEnviarRevision.setEnabled(false);
            return;
        }
        String estado = (String) tableModel.getValueAt(row, 5);
        btnEnviarRevision.setEnabled(Question.STATE_BORRADOR.equals(estado));
    }

    private void enviarARevision() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        try {
            boolean ok = questionService.submitForReview(id, autor.getLogin());
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "La pregunta [" + id + "] fue enviada a revisión.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                buscar();
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo enviar", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void volver() {
        dispose();
        if (onBack != null) {
            onBack.run();
        }
    }

    private static class EstadoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String estado = String.valueOf(value);
            setForeground(isSelected ? Color.WHITE : QuestionStateColors.colorFor(estado));
            setFont(getFont().deriveFont(Font.BOLD));
            return c;
        }
    }
}
