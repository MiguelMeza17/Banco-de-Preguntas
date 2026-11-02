package co.unicauca.bancopreguntas.preguntas.filtro;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class FiltroPorDificultad implements EstrategiaFiltro {

    @Override
    public boolean aplicaA(CriteriosBusqueda c) {
        return c.dificultad() != null;
    }

    @Override
    public Specification<Pregunta> especificacion(CriteriosBusqueda c) {
        return (root, query, cb) -> cb.equal(root.get("dificultad"), c.dificultad());
    }
}
