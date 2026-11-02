package co.unicauca.bancopreguntas.preguntas.infraestructura;

import co.unicauca.bancopreguntas.preguntas.domain.fabrica.CreadorPregunta;
import co.unicauca.bancopreguntas.preguntas.domain.fabrica.CreadorPreguntaSeleccionMultiple;
import co.unicauca.bancopreguntas.preguntas.domain.fabrica.FabricaPreguntas;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Registra los creadores de preguntas (el dominio no depende de Spring) y la info de OpenAPI. */
@Configuration
public class FabricaConfig {

    @Bean
    public CreadorPregunta creadorSeleccionMultiple() {
        return new CreadorPreguntaSeleccionMultiple();
    }

    @Bean
    public FabricaPreguntas fabricaPreguntas(List<CreadorPregunta> creadores) {
        return new FabricaPreguntas(creadores);
    }

    @Bean
    public OpenAPI apiPreguntas() {
        return new OpenAPI().info(new Info()
                .title("preguntas-service")
                .version("0.1.0")
                .description("Autoria de preguntas del banco Saber Pro: creacion, ciclo de vida y consulta."));
    }
}
