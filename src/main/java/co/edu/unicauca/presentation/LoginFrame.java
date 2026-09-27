package co.edu.unicauca.presentation;

import co.edu.unicauca.domain.User;
import co.edu.unicauca.domain.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {

    private final UserService userService;
    private JTextField txtLogin;
    private JPasswordField txtPassword;

    public LoginFrame(UserService userService) {
        this.userService = userService;
        setTitle("Inicio de Sesión - Banco Saber PRO");
        setIconImage(IconUtil.icon("bank", 32).getImage());
        setSize(380, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel lblIcon = new JLabel(IconUtil.icon("bank", 56));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel lblTitulo = new JLabel("Banco de Preguntas Saber PRO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(lblIcon);
        header.add(Box.createVerticalStrut(8));
        header.add(lblTitulo);
        header.add(Box.createVerticalStrut(15));
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 12));
        form.add(new JLabel("Login:"));
        txtLogin = new JTextField();
        form.add(txtLogin);

        form.add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField();
        form.add(txtPassword);
        root.add(form, BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridLayout(1, 2, 10, 0));
        botones.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnLogin = new JButton("Ingresar", IconUtil.icon("key", 18));
        btnLogin.setBackground(new Color(37, 99, 235));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(this::onLogin);
        botones.add(btnLogin);

        JButton btnRegister = new JButton("Registrarse", IconUtil.icon("register", 18));
        btnRegister.setFocusPainted(false);
        btnRegister.addActionListener(e -> {
            RegisterFrame registerFrame = new RegisterFrame(userService);
            registerFrame.setVisible(true);
        });
        botones.add(btnRegister);

        root.add(botones, BorderLayout.SOUTH);

        add(root);
        getRootPane().setDefaultButton(btnLogin);
    }

    private void onLogin(ActionEvent e) {
        String login = txtLogin.getText();
        String password = new String(txtPassword.getPassword());

        try {
            User user = userService.login(login, password);
            if (user != null) {
                JOptionPane.showMessageDialog(this, "Bienvenido " + user.getFullName(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
                DashboardFrame dashboard = new DashboardFrame(user, userService);
                dashboard.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Credenciales incorrectas.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
