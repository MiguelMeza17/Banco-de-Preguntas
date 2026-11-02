package co.unicauca.bancopreguntas.preguntas.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Pagina de resultados con un formato estable para el cliente (pagina base 0). */
public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas
) {
    public static <E, T> PaginaResponse<T> from(Page<E> page, Function<E, T> mapeo) {
        return new PaginaResponse<>(page.getContent().stream().map(mapeo).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
