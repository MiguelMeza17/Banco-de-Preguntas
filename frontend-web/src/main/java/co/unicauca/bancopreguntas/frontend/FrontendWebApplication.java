package co.unicauca.bancopreguntas.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Servidor de archivos estaticos del cliente web (src/main/resources/static). Toda la logica de
 * negocio esta en los microservicios; el navegador los consume por REST a traves del API Gateway.
 * Los mismos archivos se pueden publicar en cualquier servidor web (Nginx, Apache...).
 */
@SpringBootApplication
public class FrontendWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontendWebApplication.class, args);
    }
}
