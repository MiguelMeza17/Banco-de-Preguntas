package co.unicauca.bancopreguntas.preguntas.filtro;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import org.springframework.data.jpa.domain.Specification;

/**
 * Patron Strategy (GoF, comportamiento): cada criterio de filtrado es una estrategia
 * intercambiable. El servicio de busqueda no conoce los filtros concretos; solo combina las
 * estrategias que aplican a los criterios recibidos.
 * <p>
 * Cada estrategia produce una {@link Specification} para que el filtro y la paginacion se
 * resuelvan en la base de datos y no en memoria (importante para la escalabilidad).
 */
public interface EstrategiaFiltro {

    /** Indica si esta estrategia debe aplicarse con los criterios recibidos. */
    boolean aplicaA(CriteriosBusqueda criterios);

    Specification<Pregunta> especificacion(CriteriosBusqueda criterios);
}
