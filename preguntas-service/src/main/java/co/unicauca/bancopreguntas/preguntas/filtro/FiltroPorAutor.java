package co.unicauca.bancopreguntas.preguntas.filtro;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/** "Mis preguntas": solo las del autor indicado. */
@Component
public class FiltroPorAutor implements EstrategiaFiltro {

    @Override
    public boolean aplicaA(CriteriosBusqueda c) {
        return c.autorId() != null;
    }

    @Override
    public Specification<Pregunta> especificacion(CriteriosBusqueda c) {
        return (root, query, cb) -> cb.equal(root.get("autorId"), c.autorId());
    }
}
