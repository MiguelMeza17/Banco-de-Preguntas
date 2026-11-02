package co.unicauca.bancopreguntas.preguntas.filtro;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Competencia exacta, sin distinguir mayusculas. */
@Component
public class FiltroPorCompetencia implements EstrategiaFiltro {

    @Override
    public boolean aplicaA(CriteriosBusqueda c) {
        return CriteriosBusqueda.tieneTexto(c.competencia());
    }

    @Override
    public Specification<Pregunta> especificacion(CriteriosBusqueda c) {
        String competencia = c.competencia().strip().toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(root.get("competencia")), competencia);
    }
}
