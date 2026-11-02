# Escenario de calidad: Escalabilidad

El corte 2 refactoriza el monolito a microservicios orientados a eventos "para garantizar la
escalabilidad". Este escenario concreta qué significa eso para el sistema y cómo se verifica.

## Escenario

| Campo | Descripción |
|---|---|
| **Contexto** | Semana previa a las pruebas Saber Pro: los docentes de varias facultades cargan y envían a revisión preguntas al mismo tiempo, y los revisores evalúan en paralelo. El requisito RNF-17 pide soportar 500 usuarios. |
| **Fuente del estímulo** | 500 usuarios concurrentes (autores, revisores y administradores). |
| **Estímulo** | La carga sube de 100 a 500 usuarios en 10 minutos, con un pico de 50 peticiones por segundo sobre la consulta de preguntas y de 10 evaluaciones por segundo. |
| **Artefacto** | `preguntas-service`, `revision-service` y el bus de eventos. |
| **Ambiente** | En producción, en operación normal. |
| **Respuesta** | El sistema escala horizontalmente agregando instancias de `preguntas-service` y `revision-service` detrás del gateway, sin cambiar código ni detener el servicio. Los eventos se reparten entre las instancias y ninguno se pierde ni se procesa dos veces con efecto doble. |
| **Medición de la respuesta** | El 95 % de las consultas responde en menos de 3 segundos (RNF-04) con 500 usuarios; 0 eventos perdidos; el tiempo de procesamiento de eventos crece menos de 20 % al duplicar la carga si se duplican las instancias. |
| **Resultado esperado** | Con 2 instancias de cada servicio de negocio se atienden 500 usuarios cumpliendo la medición; agregar una instancia toma menos de 5 minutos. |

## Tácticas de arquitectura que lo hacen posible

1. **Servicios sin estado + base de datos por servicio.** Ninguna instancia guarda sesión en memoria,
   así que cualquier instancia atiende cualquier petición. Cada servicio escala por separado según su
   propia carga (las consultas de preguntas son mucho más frecuentes que los registros de usuario).
2. **Comunicación asíncrona por eventos.** Las tareas que no necesita el usuario de inmediato
   (notificar, actualizar estados en otro servicio) salen de la petición HTTP y se procesan por el bus.
   Un pico de evaluaciones no hace más lenta la respuesta al revisor.
3. **Suscripciones durables compartidas.** Todas las instancias de un servicio comparten la misma
   suscripción (`preguntas-service.pregunta-evaluada`, ...), por eso el bus reparte los mensajes entre
   ellas (consumidores en competencia) y los guarda si todas están caídas.
4. **Consumidores idempotentes.** Si un evento se entrega dos veces (por un reintento), el resultado es
   el mismo: por ejemplo, `registrarInicioRevision` no hace nada si la pregunta ya está en revisión.
5. **Paginación y filtros en la base de datos.** El patrón Strategy genera `Specification` de JPA, así
   que la consulta trae solo la página pedida (máximo 50 elementos) en lugar de filtrar en memoria.
6. **Punto de entrada único.** El frontend solo conoce el gateway; para escalar se cambia la URL de un
   servicio por la de un balanceador (o `lb://nombre` con un registro de servicios) en
   `api-gateway/src/main/resources/application.yml`.

## Cómo demostrarlo en el equipo local

1. Arrancar el sistema con `iniciar.ps1`.
2. Levantar una segunda instancia de `revision-service` en otro puerto, usando la misma base:
   `java -jar revision-service/target/revision-service-0.1.0.jar --server.port=9083`
   (ejecutado desde la carpeta `revision-service`).
3. Evaluar varias preguntas y revisar los logs: los eventos `RevisoresAsignados` y `PreguntaEvaluada`
   que consume `preguntas-service` se procesan una sola vez, y si se detiene `notificaciones-service`
   y se vuelve a iniciar, recibe los avisos pendientes.
4. Para la medición de tiempos se puede usar JMeter o `hey` contra
   `GET http://localhost:8080/api/preguntas?pagina=0&tamano=10`.
