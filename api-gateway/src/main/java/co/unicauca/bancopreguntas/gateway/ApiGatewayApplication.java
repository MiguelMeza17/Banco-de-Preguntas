package co.unicauca.bancopreguntas.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway: unico punto de entrada para el frontend. Enruta /api/** a cada microservicio
 * (rutas en application.yml) y expone /api/fachada/** con operaciones que combinan varios servicios.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
