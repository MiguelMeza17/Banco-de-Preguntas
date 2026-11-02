package co.unicauca.bancopreguntas.preguntas.service;

import co.unicauca.bancopreguntas.preguntas.domain.Pregunta;
import co.unicauca.bancopreguntas.preguntas.domain.ReglaNegocioException;
import co.unicauca.bancopreguntas.preguntas.domain.estado.CodigoEstado;
import co.unicauca.bancopreguntas.preguntas.domain.fabrica.FabricaPreguntas;
import co.unicauca.bancopreguntas.preguntas.domain.fabrica.FuenteDatosPregunta;
import co.unicauca.bancopreguntas.preguntas.evento.PreguntaCreada;
import co.unicauca.bancopreguntas.preguntas.evento.PreguntaEnviadaARevision;
import co.unicauca.bancopreguntas.preguntas.evento.PublicadorEventos;
import co.unicauca.bancopreguntas.preguntas.filtro.CombinadorFiltros;
import co.unicauca.bancopreguntas.preguntas.filtro.CriteriosBusqueda;
import co.unicauca.bancopreguntas.preguntas.repository.PreguntaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso del contexto de Autoria: crear y editar preguntas (HU-1), gestionar su ciclo de
 * vida (HU-2) y consultarlas con filtros y paginacion (HU-3).
 */
@Service
public class PreguntaService {

    public static final int TAMANO_MAXIMO_PAGINA = 50;

    private final PreguntaRepository repository;
    private final FabricaPreguntas fabrica;
    private final CombinadorFiltros filtros;
    private final PublicadorEventos eventos;

    public PreguntaService(PreguntaRepository repository, FabricaPreguntas fabrica,
                           CombinadorFiltros filtros, PublicadorEventos eventos) {
        this.repository = repository;
        this.fabrica = fabrica;
        this.filtros = filtros;
        this.eventos = eventos;
    }

    @Transactional
    public Pregunta crear(FuenteDatosPregunta datos, Long autorId) {
        if (autorId == null) {
            throw new ReglaNegocioException("La pregunta debe tener un autor.");
        }
        Pregunta guardada = repository.save(fabrica.crear(datos, autorId));
        eventos.publicar(PreguntaCreada.de(guardada));
        return guardada;
    }

    /** RF-06: modificar una pregunta mientras este en Borrador. */
    @Transactional
    public Pregunta actualizar(Long id, FuenteDatosPregunta datos, Long usuarioId) {
        Pregunta pregunta = buscar(id);
        Pregunta cambios = fabrica.crear(datos, pregunta.getAutorId());
        pregunta.actualizarContenido(cambios, usuarioId);
        return repository.save(pregunta);
    }

    @Transactional(readOnly = true)
    public Pregunta buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new PreguntaNoEncontradaException(id));
    }

    /** RF-07: consulta paginada; los filtros se resuelven con las estrategias registradas. */
    @Transactional(readOnly = true)
    public Page<Pregunta> buscar(CriteriosBusqueda criterios, int pagina, int tamano) {
        int tamanoValido = Math.min(Math.max(tamano, 1), TAMANO_MAXIMO_PAGINA);
        PageRequest solicitud = PageRequest.of(Math.max(pagina, 0), tamanoValido,
                Sort.by(Sort.Direction.DESC, "fechaActualizacion").and(Sort.by("id")));
        return repository.findAll(filtros.combinar(criterios), solicitud);
    }

    /**
     * Cambia el estado por solicitud de un usuario (PATCH /preguntas/{id}/estado).
     * Las transiciones En revision, Aprobada y Rechazada las decide el servicio de revision
     * por medio de eventos, por eso no se aceptan aqui.
     */
    @Transactional
    public Pregunta cambiarEstado(Long id, CodigoEstado destino, Long usuarioId) {
        if (destino == null) {
            throw new ReglaNegocioException("Debe indicar el estado destino.");
        }
        Pregunta pregunta = buscar(id);
        switch (destino) {
            case PENDIENTE_REVISION -> pregunta.enviarARevision(usuarioId);
            case BORRADOR -> pregunta.devolverABorrador(usuarioId);
            case PUBLICADA -> pregunta.publicar();
            case ARCHIVADA -> pregunta.archivar();
            case EN_REVISION, APROBADA, RECHAZADA -> throw new ReglaNegocioException(
                    "El estado " + destino.getNombre() + " lo asigna el proceso de revision por pares.");
        }
        Pregunta guardada = repository.save(pregunta);
        if (destino == CodigoEstado.PENDIENTE_REVISION) {
            eventos.publicar(PreguntaEnviadaARevision.de(guardada));
        }
        return guardada;
    }

    /** Reaccion al evento RevisoresAsignados. Idempotente: si ya esta en revision no hace nada. */
    @Transactional
    public void registrarInicioRevision(Long preguntaId) {
        Pregunta pregunta = buscar(preguntaId);
        if (pregunta.getEstado() == CodigoEstado.PENDIENTE_REVISION) {
            pregunta.iniciarRevision();
            repository.save(pregunta);
        }
    }

    /** Reaccion al dictamen final de la revision por pares. Idempotente. */
    @Transactional
    public void registrarDictamen(Long preguntaId, String resultado) {
        Pregunta pregunta = buscar(preguntaId);
        if (pregunta.getEstado() != CodigoEstado.EN_REVISION) {
            return;
        }
        if ("APROBADA".equals(resultado)) {
            pregunta.aprobar();
        } else if ("RECHAZADA".equals(resultado)) {
            pregunta.rechazar();
        } else {
            throw new ReglaNegocioException("Dictamen desconocido: " + resultado + ".");
        }
        repository.save(pregunta);
    }
}
