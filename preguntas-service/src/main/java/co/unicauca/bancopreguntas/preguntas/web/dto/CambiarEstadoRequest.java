package co.unicauca.bancopreguntas.preguntas.web.dto;

import co.unicauca.bancopreguntas.preguntas.domain.estado.CodigoEstado;
import io.swagger.v3.oas.annotations.media.Schema;

/** Cuerpo de PATCH /preguntas/{id}/estado. */
public record CambiarEstadoRequest(
        @Schema(description = "PENDIENTE_REVISION, BORRADOR, PUBLICADA o ARCHIVADA", example = "PENDIENTE_REVISION")
        CodigoEstado estado,
        @Schema(description = "Usuario que solicita el cambio", example = "2")
        Long usuarioId
) {
}
