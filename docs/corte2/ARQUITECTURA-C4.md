# Arquitectura C4 — Corte 2: microservicios orientados a eventos

## Nivel 1 — Contexto del sistema

```mermaid
flowchart TB
    autor["👤 Autor de preguntas<br/>Crea y corrige preguntas"]
    admin["👤 Administrador<br/>Asigna revisores, publica y archiva"]
    revisor["👤 Revisor<br/>Evalúa las preguntas asignadas"]
    docente["👤 Docente / Estudiante<br/>Consulta el banco publicado"]
    sistema["🖥️ Sistema Banco de Preguntas Saber Pro<br/>Gestión, validación y revisión de preguntas"]
    correo["✉️ Servicio de correo<br/>(simulado en el corte 2)"]

    autor --> sistema
    admin --> sistema
    revisor --> sistema
    docente --> sistema
    sistema -. "avisos" .-> correo
```

## Nivel 2 — Contenedores

```mermaid
flowchart TB
    nav["Navegador web"]

    subgraph sistema["Sistema Banco de Preguntas"]
        front["frontend-web<br/>HTML, CSS y JavaScript<br/>:3000"]
        gw["api-gateway<br/>Spring Cloud Gateway<br/>Enrutamiento + Fachada<br/>:8080"]

        us["usuarios-service<br/>Spring Boot<br/>:8081"]
        ps["preguntas-service<br/>Spring Boot<br/>:8082"]
        rs["revision-service<br/>Spring Boot<br/>:8083"]
        ns["notificaciones-service<br/>Spring Boot<br/>:8085"]

        bus[("event-bus<br/>ActiveMQ Artemis<br/>tcp :61616")]

        dbu[("BD usuarios")]
        dbp[("BD preguntas")]
        dbr[("BD revisión")]
        dbn[("BD notificaciones")]
    end

    nav -- "HTTP" --> front
    nav -- "REST/JSON" --> gw
    gw -- "/api/usuarios/**" --> us
    gw -- "/api/preguntas/**" --> ps
    gw -- "/api/revisiones/**" --> rs
    gw -- "/api/notificaciones/**" --> ns

    us --- dbu
    ps --- dbp
    rs --- dbr
    ns --- dbn

    ps -- "publica PreguntaCreada,<br/>PreguntaEnviadaARevision" --> bus
    rs -- "publica RevisoresAsignados,<br/>PreguntaEvaluada" --> bus
    bus -- "consume eventos de revisión" --> ps
    bus -- "consume todos los eventos" --> ns
```

| Contenedor | Tecnología | Responsabilidad |
|---|---|---|
| `frontend-web` | HTML, CSS y JavaScript sin frameworks, servido por Spring Boot | Interfaz web por rol. Solo conoce la URL del gateway. |
| `api-gateway` | Spring Cloud Gateway (WebFlux) | Punto de entrada único, CORS y fachada de operaciones compuestas. |
| `usuarios-service` | Spring Boot, JPA, Argon2id | Registro, autenticación y roles. |
| `preguntas-service` | Spring Boot, JPA, JMS, OpenAPI | Autoría, validación estructural, ciclo de vida y consulta paginada. |
| `revision-service` | Spring Boot, JPA, JMS, OpenAPI | Asignación de revisores, evaluación e historial. |
| `notificaciones-service` | Spring Boot, JPA, JMS | Bandeja de avisos generada a partir de los eventos. |
| `event-bus` | ActiveMQ Artemis embebido | Tópicos con suscripciones durables compartidas. |
| Bases de datos | H2 en archivo (desarrollo) / PostgreSQL (perfil `postgres`) | Una por servicio: ningún servicio lee la base de otro. |

## Nivel 3 — Componentes

### preguntas-service

```mermaid
flowchart TB
    ctrl["PreguntaController<br/>REST + OpenAPI"]
    adap["PreguntaRequestAdapter<br/>Adapter: DTO → FuenteDatosPregunta"]
    srv["PreguntaService<br/>casos de uso"]
    fab["FabricaPreguntas + CreadorPregunta<br/>Factory Method"]
    val["ValidadorEstructural<br/>RF-08 a RF-13"]
    est["EstadoPregunta (Borrador, Pendiente, ...)<br/>State"]
    fil["CombinadorFiltros + EstrategiaFiltro<br/>Strategy"]
    repo["PreguntaRepository<br/>JPA + Specifications"]
    pub["PublicadorEventos → ReenvioEventosAlBus<br/>Observer (después del commit)"]
    cons["ConsumidorEventosRevision<br/>@JmsListener"]

    ctrl --> adap --> srv
    srv --> fab --> val
    srv --> est
    srv --> fil --> repo
    srv --> repo
    srv --> pub
    cons --> srv
```

### revision-service

```mermaid
flowchart TB
    ctrl["RevisionController"]
    asig["AsignacionRevisionService"]
    eva["EvaluacionRevisionService<br/>calcula el dictamen final"]
    con["ConsultaRevisionService<br/>mis asignadas, historial"]
    dom["Revision + EstadoRevision<br/>reglas del dictamen"]
    repo["RevisionRepository"]
    pub["PublicadorEventos → ReenvioEventosAlBus"]

    ctrl --> asig & eva & con
    asig --> repo
    eva --> dom
    eva --> repo
    con --> repo
    asig --> pub
    eva --> pub
```

### api-gateway

```mermaid
flowchart TB
    rutas["Rutas /api/** (application.yml)<br/>StripPrefix hacia cada servicio"]
    fc["FachadaController<br/>/api/fachada/**"]
    fa["BancoPreguntasFacade<br/>Facade"]
    cli["ClientesMicroservicios<br/>WebClient por servicio"]
    cors["CorsWebFilter"]

    cors --> rutas
    cors --> fc --> fa --> cli
```

### notificaciones-service y usuarios-service

- **notificaciones-service:** `ConsumidorEventos` (un `@JmsListener` por tópico) → `NotificacionService`
  (decide destinatarios) → `NotificacionRepository` y `CanalNotificacion` (correo simulado).
  `PreguntaConocida` es la proyección local *pregunta → autor*.
- **usuarios-service:** `UsuarioController` → `UsuarioService` → `UsuarioRepository`, con
  `HasherPassword` (Argon2id) y `ValidadorPassword`. `DatosIniciales` crea los usuarios de demostración.

## Flujo principal (HU-2, HU-4 y HU-5)

```mermaid
sequenceDiagram
    actor Autor
    actor Admin
    actor Revisor
    participant GW as api-gateway
    participant P as preguntas-service
    participant R as revision-service
    participant B as event-bus
    participant N as notificaciones-service

    Autor->>GW: PATCH /api/preguntas/1/estado (PENDIENTE_REVISION)
    GW->>P: PATCH /preguntas/1/estado
    P-->>B: PreguntaEnviadaARevision
    B-->>N: aviso al administrador
    Admin->>GW: POST /api/fachada/preguntas/1/revisores
    GW->>P: GET /preguntas/1 (¿pendiente?)
    GW->>R: POST /revisiones/asignar
    R-->>B: RevisoresAsignados
    B-->>P: pregunta → En revisión
    B-->>N: aviso a cada revisor
    Revisor->>GW: POST /api/revisiones/7/evaluar
    GW->>R: POST /revisiones/7/evaluar
    R-->>B: PreguntaEvaluada (dictamen final)
    B-->>P: pregunta → Aprobada / Rechazada
    B-->>N: aviso al autor y al administrador
```

## Patrones de diseño GoF aplicados

| Patrón | Tipo | Dónde | Para qué |
|---|---|---|---|
| Builder | Creacional | `Pregunta.Builder` (heredado del corte 1) | Armar una pregunta paso a paso. |
| Factory Method | Creacional | `CreadorPregunta` / `CreadorPreguntaSeleccionMultiple` | Cada tipo de pregunta define cómo se construye; el algoritmo común valida y entrega en Borrador. |
| State | Comportamiento | `EstadoPregunta` y sus 7 estados | Cada estado decide sus transiciones válidas (RF-14, RF-15). |
| Strategy | Comportamiento | `EstrategiaFiltro` y sus 6 filtros | Filtros intercambiables combinados en una sola consulta paginada. |
| Observer | Comportamiento | Eventos de dominio + bus + `@JmsListener` | Los servicios reaccionan a lo que pasa en otros sin acoplarse. |
| Adapter | Estructural | `PreguntaRequestAdapter` | Adapta el DTO REST a la interfaz que espera el dominio. |
| Facade | Estructural | `BancoPreguntasFacade` | Operaciones compuestas sobre 3 servicios con una sola llamada. |

## Decisiones de arquitectura del corte 2

- **Bus de eventos con Artemis embebido:** permite ejecutar todo el sistema solo con Java (sin Docker,
  RabbitMQ ni Kafka). En producción se usa un broker Artemis independiente cambiando
  `spring.artemis.broker-url`.
- **Suscripciones durables compartidas:** si un servicio está caído, sus eventos esperan en el bus;
  si hay varias instancias, se reparten los mensajes (ver el escenario de escalabilidad).
- **Eventos publicados después del commit:** `@TransactionalEventListener` evita anunciar cambios que
  luego se deshacen.
- **Base de datos por servicio:** H2 en archivo para desarrollo y PostgreSQL con el perfil `postgres`.
- **Contratos por nombre lógico:** cada mensaje lleva en la cabecera `_tipo` el nombre del evento
  (`PreguntaCreada`, ...). Cada servicio define su propia clase con solo los campos que usa, así un
  productor puede agregar campos sin romper a los consumidores (ver [EVENTOS.md](EVENTOS.md)).
