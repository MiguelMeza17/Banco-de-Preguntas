package co.unicauca.bancopreguntas.preguntas.filtro;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Texto libre buscado en titulo, enunciado y tema (como en el Corte 1). */
@Component
public class FiltroPorTexto implements EstrategiaFiltro {

    @Override
    public boolean aplicaA(CriteriosBusqueda c) {
        return CriteriosBusqueda.tieneTexto(c.texto());
    }

    @Override
    public Specification<Pregunta> especificacion(CriteriosBusqueda c) {
        String patron = "%" + c.texto().strip().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("titulo")), patron, '\\'),
                cb.like(cb.lower(root.get("enunciado")), patron, '\\'),
                cb.like(cb.lower(root.get("tema")), patron, '\\'));
    }
}
