# Contrato de eventos del bus

Mensajes JSON (texto) publicados en tópicos de ActiveMQ Artemis. La cabecera `_tipo` lleva el nombre
lógico del evento. Los consumidores usan suscripciones durables compartidas con el nombre
`<servicio>.<evento>`.

| Evento | Tópico | Publica | Consumen |
|---|---|---|---|
| `PreguntaCreada` | `banco.preguntas.creada` | preguntas-service | notificaciones-service |
| `PreguntaEnviadaARevision` | `banco.preguntas.enviada-revision` | preguntas-service | notificaciones-service |
| `RevisoresAsignados` | `banco.revisiones.revisores-asignados` | revision-service | preguntas-service, notificaciones-service |
| `PreguntaEvaluada` | `banco.revisiones.pregunta-evaluada` | revision-service | preguntas-service, notificaciones-service |

## Campos

```json
// PreguntaCreada
{ "preguntaId": 1, "autorId": 2, "titulo": "...", "competencia": "...", "tema": "...", "ocurridoEn": "2026-11-01T03:49:00Z" }

// PreguntaEnviadaARevision
{ "preguntaId": 1, "autorId": 2, "titulo": "...", "ocurridoEn": "..." }

// RevisoresAsignados
{ "preguntaId": 1, "revisionIds": [1, 2], "revisorIds": [3, 4], "ocurridoEn": "..." }

// PreguntaEvaluada
{
  "revisionId": 2, "preguntaId": 1, "revisorId": 4,
  "resultado": "RECHAZADA", "observaciones": "La opcion B tambien puede ser correcta",
  "destinatarios": ["AUTOR", "ADMINISTRADOR"],
  "revisionCompleta": true, "resultadoPregunta": "RECHAZADA",
  "ocurridoEn": "..."
}
```

`revisionCompleta` es `true` cuando todos los revisores asignados emitieron su dictamen. En ese caso
`resultadoPregunta` es `RECHAZADA` si al menos uno rechazó, o `APROBADA` si todos aprobaron.

## Reglas para evolucionar el contrato

- Agregar un campo es compatible: los consumidores ignoran los campos que no conocen.
- Quitar o renombrar un campo exige versionar el evento (por ejemplo `PreguntaEvaluadaV2`) y
  publicar ambos mientras los consumidores migran.
- Los consumidores son idempotentes: procesar dos veces el mismo evento no cambia el resultado.
