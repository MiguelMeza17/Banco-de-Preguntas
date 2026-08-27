# Documento de Arquitectura — Corte 1

Sistema para la Gestión, Validación y Administración de un Banco de Preguntas para la
Preparación de las Pruebas Saber Pro · Universidad del Cauca · Ingeniería de Software II

## 1. Decisiones de arquitectura

### 1.1 Tecnología de back-end: Java + Spring Boot

Para el Corte 1 se plantea un monolito en capas (MVC), por lo que Spring Boot es adecuado:
provee un contenedor de inyección de dependencias que facilita separar capas (controlador,
servicio, repositorio), cumpliendo RNF-12 (separación lógica/UI/persistencia) y RNF-10
(SOLID). Spring Data JPA reduce el código repetitivo de acceso a datos, y Spring Security
deja lista la autenticación por roles que piden RNF-06 y RNF-07.

**Ventaja:** Ecosistema maduro; migración natural a microservicios en el Corte 2 (Spring
Cloud) sin cambiar de lenguaje.

**Desventaja:** Curva de aprendizaje mayor que Java plano; para un proyecto pequeño puede
sentirse "pesado" al inicio.

### 1.2 Tecnología de front-end: JavaFX en código Java puro, con Maven

Se descarta NetBeans y Scene Builder como dependencia obligatoria: el proyecto se construye
con Maven estándar (`pom.xml`) y el plugin `javafx-maven-plugin`, programado directamente en
Java sin FXML. Se edita en Visual Studio Code con la extensión "Extension Pack for Java", de
forma que todo el equipo trabaja con el mismo editor y el mismo sistema de construcción para
front-end y back-end.

**Ventaja:** Mismo editor y build tool para todo el proyecto; multiplataforma sin fricción;
no depende de un IDE específico.

**Desventaja:** Configurar JavaFX con Maven toma un poco más al inicio que un proyecto ya
armado en un IDE.

### 1.3 Tipo de aplicación: aplicación de escritorio cliente-servidor

El sistema distingue 5 roles (administrador, autor, revisor, docente, estudiante) operando
simultáneamente y debe soportar 100 usuarios concurrentes (RNF-05), lo que exige un backend
centralizado. Se elige una aplicación de escritorio cliente-servidor: cada usuario ejecuta el
cliente JavaFX en su máquina, y todos los clientes consumen la misma API Java (Spring Boot).

**Ventaja:** Cumple la sugerencia tecnológica del curso (Java, Swing/JavaFX) y la necesidad
real de multiusuario.

**Desventaja:** Más complejo de desplegar que una aplicación de escritorio aislada: hay que
levantar cliente y servidor por separado.

### 1.4 Motor de base de datos: PostgreSQL

Entre las dos opciones sugeridas por el documento del proyecto (MariaDB/PostgreSQL), se elige
PostgreSQL por su mejor soporte de tipos de datos avanzados (JSON, arrays), útil si se
necesita flexibilidad para nuevos tipos de pregunta o competencias (RNF-13), y por su
reputación más sólida en escenarios de alta concurrencia (RNF-17: 500 usuarios).

**Ventaja:** Extensible, gratuito, buen soporte con Spring Data JPA.

**Desventaja:** MariaDB es ligeramente más simple de instalar/administrar para un equipo sin
experiencia previa.

## 2. Historias épicas, historias de usuario y prototipos

Las historias épicas, historias de usuario, criterios de aceptación y prototipos de interfaz
de usuario ya fueron elaborados en la Actividad 1 (documento "Prototipo 1" y hoja de cálculo
de plantilla de historias de usuario, `HistoriasEpicas.xlsx` / `HistoriasUsuario.xlsx`). Se
referencian aquí y no se repiten para evitar duplicar contenido.

## 3. Mini-QAW — Atributos de calidad más importantes

Con base en los requisitos no funcionales del proyecto de curso y en el roadmap de los tres
cortes (que asocia explícitamente el Corte 1 con la modificabilidad y el Corte 3 con la
seguridad de autenticación/autorización), se identifican los siguientes dos atributos de
calidad como los más importantes para el sistema:

- **Modificabilidad** — El Corte 1 del proyecto exige explícitamente "garantizar la
  modificabilidad" mediante principios y patrones de diseño (RNF-10 a RNF-14): el sistema
  debe poder incorporar nuevos tipos de pregunta, criterios de revisión y competencias sin
  modificar los componentes existentes.
- **Seguridad** — El sistema maneja 5 roles con permisos distintos sobre un banco de
  preguntas académico (RNF-06 a RNF-09): autenticación, control de acceso por rol,
  contraseñas con hash seguro y auditoría de cada acción son requisitos explícitos y críticos
  para la integridad del proceso de revisión.

## 4. Escenarios de calidad

### 4.1 Escenario de Modificabilidad

| Campo | Descripción |
|---|---|
| Fuente del estímulo | Un desarrollador del equipo. |
| Estímulo | Necesita agregar un nuevo tipo de pregunta (por ejemplo, de emparejamiento), distinto al de selección múltiple. |
| Artefacto | El módulo de gestión del banco de preguntas. |
| Ambiente | En tiempo de diseño, durante el desarrollo de una nueva iteración. |
| Respuesta | El desarrollador agrega una nueva clase que implementa la interfaz de tipo de pregunta, sin modificar las clases existentes de validación estructural ni el flujo de revisión. |
| Medida de respuesta | El cambio no requiere modificar más de 2 clases existentes y toma menos de 4 horas de trabajo. |

### 4.2 Escenario de Seguridad

| Campo | Descripción |
|---|---|
| Fuente del estímulo | Un usuario autenticado con rol "Estudiante". |
| Estímulo | Intenta acceder a la funcionalidad de aprobar o rechazar una pregunta, reservada al rol "Revisor". |
| Artefacto | El módulo de autorización de la API. |
| Ambiente | En tiempo de ejecución, sistema en producción. |
| Respuesta | El sistema rechaza la petición (403), registra el intento en el log de auditoría y no revela información adicional sobre el recurso solicitado. |
| Medida de respuesta | El 100% de los intentos no autorizados son bloqueados y quedan registrados en menos de 1 segundo. |

## 5. Tácticas de arquitectura

### 5.1 Táctica para Modificabilidad: Encapsular (Encapsulate)

Consiste en ocultar los detalles internos de un módulo detrás de una interfaz explícita, de
forma que el resto del sistema dependa solo de esa interfaz y no de la implementación
concreta. Se elige porque permite crear una interfaz `TipoPregunta` que oculta cómo se valida
cada tipo específico de pregunta: agregar un tipo nuevo significa crear una clase adicional,
sin tocar el código que ya usa esa interfaz, exactamente lo que pide RNF-13.

### 5.2 Táctica para Seguridad: Autorizar actores (Authorize Actors)

Consiste en verificar, para cada actor ya autenticado, si tiene permiso para realizar la
acción o acceder al recurso solicitado, normalmente mediante control de acceso basado en
roles (RBAC). Se elige porque el sistema tiene 5 roles con permisos claramente distintos
(RF-03) y RNF-07 exige explícitamente restringir el acceso según el rol; Spring Security
implementa esta táctica de forma directa mediante anotaciones sobre cada endpoint.

## Referencias

Bass, L., Clements, P., & Kazman, R. (2021). *Software Architecture in Practice* (4th ed.).
Addison-Wesley.

Universidad del Cauca. (2026). *Proyecto de clase 2026.2: Sistema para la Gestión,
Validación y Administración de un Banco de Preguntas para la Preparación de las Pruebas
Saber Pro*. Ingeniería de Software II.
