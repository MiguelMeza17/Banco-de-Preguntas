package co.unicauca.bancopreguntas.preguntas.filtro;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Tema exacto, sin distinguir mayusculas. */
@Component
public class FiltroPorTema implements EstrategiaFiltro {

    @Override
    public boolean aplicaA(CriteriosBusqueda c) {
        return CriteriosBusqueda.tieneTexto(c.tema());
    }

    @Override
    public Specification<Pregunta> especificacion(CriteriosBusqueda c) {
        String tema = c.tema().strip().toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(root.get("tema")), tema);
    }
}
