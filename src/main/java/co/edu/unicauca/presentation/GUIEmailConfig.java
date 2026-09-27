package co.edu.unicauca.presentation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Pantalla de configuración del correo remitente usado para las notificaciones
 * de asignación de revisores (HU04). Es solo una vista: no valida ni envía
 * correo de verdad, únicamente confirma visualmente los datos ingresados.
 */
public class GUIEmailConfig extends JFrame {

    private final Runnable onBack;

    private JTextField txtGmail;
    private JPasswordField txtPassword;
    private JLabel lblEstado;

    public GUIEmailConfig(Runnable onBack) {
        this.onBack = onBack;
        initComponents();
    }

    private void initComponents() {
        setTitle("Configurar correo remitente");
        setIconImage(IconUtil.icon("settings", 32).getImage());
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titlePanel.add(new JLabel(IconUtil.icon("email", 26)));
        JLabel titulo = new JLabel("Cuenta que enviará las notificaciones a los revisores");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titlePanel.add(titulo);
        panel.add(titlePanel, gbc);
        gbc.gridwidth = 1;

        y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Correo Gmail:"), gbc);
        txtGmail = new JTextField();
        gbc.gridx = 1; panel.add(txtGmail, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Contraseña de aplicación:"), gbc);
        txtPassword = new JPasswordField();
        gbc.gridx = 1; panel.add(txtPassword, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnVolver = new JButton("Volver al tablero", IconUtil.icon("home", 16));
        btnVolver.addActionListener(e -> volver());
        JButton btnGuardar = new JButton("Guardar y enviar prueba", IconUtil.icon("email", 16));
        btnGuardar.setBackground(new Color(16, 129, 79));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> onGuardar());
        botones.add(btnVolver);
        botones.add(btnGuardar);
        panel.add(botones, gbc);

        y++;
        gbc.gridy = y;
        lblEstado = new JLabel(" ");
        panel.add(lblEstado, gbc);

        add(panel);
    }

    private void onGuardar() {
        String gmail = txtGmail.getText().trim();
        char[] password = txtPassword.getPassword();

        if (gmail.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese el correo Gmail y la contraseña de aplicación.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        lblEstado.setForeground(new Color(16, 129, 79));
        lblEstado.setText("Correo de prueba enviado desde " + gmail);
        JOptionPane.showMessageDialog(this,
                "Configuración guardada.\nCorreo de prueba enviado desde " + gmail + ".",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void volver() {
        dispose();
        if (onBack != null) {
            onBack.run();
        }
    }
}
