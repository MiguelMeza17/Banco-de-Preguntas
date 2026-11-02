package co.unicauca.bancopreguntas.preguntas.evento;

import java.util.List;

/** Evento que publica revision-service; aqui solo se leen los campos que este servicio usa. */
public record RevisoresAsignados(Long preguntaId, List<Long> revisorIds) {
}
