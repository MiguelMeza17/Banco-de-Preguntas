package co.edu.unicauca.access;

import co.edu.unicauca.domain.IEmailNotifier;
import co.edu.unicauca.domain.Question;
import co.edu.unicauca.domain.User;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;

public class SmtpEmailNotifier implements IEmailNotifier {

    private static final String CONFIG_FILE = "email.properties";

    private final Properties config;

    public SmtpEmailNotifier() {
        this.config = loadConfig();
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar " + CONFIG_FILE + ": " + e.getMessage());
        }
        return props;
    }

    @Override
    public void sendReviewAssignmentEmail(User reviewer, Question question) throws MessagingException {
        String host = config.getProperty("smtp.host", "").trim();
        String from = config.getProperty("smtp.from", "").trim();
        String username = config.getProperty("smtp.username", "").trim();

        if (host.isEmpty() || reviewer.getEmail() == null || reviewer.getEmail().isBlank()) {
            System.out.println("[EMAIL] Notificación omitida (SMTP no configurado o revisor sin correo): "
                    + reviewer.getLogin() + " -> pregunta " + question.getId());
            return;
        }

        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", host);
        mailProps.put("mail.smtp.port", config.getProperty("smtp.port", "587"));
        mailProps.put("mail.smtp.auth", config.getProperty("smtp.auth", "true"));
        mailProps.put("mail.smtp.starttls.enable", config.getProperty("smtp.starttls", "true"));

        String password = config.getProperty("smtp.password", "");

        Session session = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from.isEmpty() ? username : from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(reviewer.getEmail()));
        message.setSubject("Nueva pregunta asignada para revisión - Banco Saber Pro");
        message.setText("Hola " + reviewer.getFullName() + ",\n\n"
                + "Se te ha asignado la siguiente pregunta para revisión:\n\n"
                + "ID: " + question.getId() + "\n"
                + "Título: " + question.getNombre() + "\n"
                + "Tema: " + (question.getTema() != null ? question.getTema() : "-") + "\n"
                + "Competencia: " + (question.getCompetencia() != null ? question.getCompetencia() : "-") + "\n\n"
                + "Por favor ingresa al sistema para revisarla.\n\n"
                + "Banco de Preguntas Saber Pro - Universidad del Cauca");

        Transport.send(message);
        System.out.println("[EMAIL] Notificación enviada a " + reviewer.getEmail());
    }
}
