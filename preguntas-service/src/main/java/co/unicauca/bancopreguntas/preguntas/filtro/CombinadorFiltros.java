package co.unicauca.bancopreguntas.preguntas.filtro;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Contexto del patron Strategy: recibe todas las estrategias registradas y combina con AND las
 * que aplican. Un filtro nuevo es una clase nueva anotada con {@code @Component}; esta clase no
 * cambia.
 */
@Component
public class CombinadorFiltros {

    private final List<EstrategiaFiltro> estrategias;

    public CombinadorFiltros(List<EstrategiaFiltro> estrategias) {
        this.estrategias = List.copyOf(estrategias);
    }

    public Specification<Pregunta> combinar(CriteriosBusqueda criterios) {
        Specification<Pregunta> resultado = (root, query, cb) -> cb.conjunction();
        for (EstrategiaFiltro estrategia : estrategias) {
            if (estrategia.aplicaA(criterios)) {
                resultado = resultado.and(estrategia.especificacion(criterios));
            }
        }
        return resultado;
    }

    /** Cuantas estrategias aplican; util para pruebas y trazas. */
    public long contarAplicables(CriteriosBusqueda criterios) {
        return estrategias.stream().filter(e -> e.aplicaA(criterios)).count();
    }
}
