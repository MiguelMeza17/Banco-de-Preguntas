package co.unicauca.bancopreguntas.revision.web.dto;

import java.util.List;

public record AsignarRevisionRequest(
        Long preguntaId,
        List<Long> revisorIds
) {
}
