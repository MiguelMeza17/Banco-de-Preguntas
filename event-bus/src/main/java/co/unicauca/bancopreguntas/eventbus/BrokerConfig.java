package co.unicauca.bancopreguntas.eventbus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expone el broker embebido por TCP. Por defecto Artemis embebido solo acepta conexiones
 * dentro de la misma JVM; el aceptador TCP permite que los demas procesos se conecten.
 */
@Configuration
public class BrokerConfig {

    @Bean
    public ArtemisConfigurationCustomizer aceptadorTcp(@Value("${bus.puerto:61616}") int puerto,
                                                       @Value("${bus.directorio-datos:./data/broker}") String datos) {
        return configuration -> {
            try {
                configuration.addAcceptorConfiguration("tcp", "tcp://0.0.0.0:" + puerto);
            } catch (Exception e) {
                throw new IllegalStateException("No se pudo configurar el aceptador TCP del bus", e);
            }
            configuration.setSecurityEnabled(false);
            configuration.setPersistenceEnabled(true);
            configuration.setJournalDirectory(datos + "/journal");
            configuration.setBindingsDirectory(datos + "/bindings");
            configuration.setLargeMessagesDirectory(datos + "/large-messages");
            configuration.setPagingDirectory(datos + "/paging");
        };
    }
}
