package co.unicauca.bancopreguntas.preguntas.filtro;

import co.unicauca.bancopreguntas.preguntas.domain.Dificultad;
import co.unicauca.bancopreguntas.preguntas.domain.estado.CodigoEstado;

/**
 * Filtros opcionales de la consulta de preguntas (RF-07). Un campo nulo o vacio significa
 * "no filtrar por este criterio".
 */
public record CriteriosBusqueda(
        Long autorId,
        CodigoEstado estado,
        String tema,
        String competencia,
        Dificultad dificultad,
        String texto
) {
    public static CriteriosBusqueda vacios() {
        return new CriteriosBusqueda(null, null, null, null, null, null);
    }

    static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
