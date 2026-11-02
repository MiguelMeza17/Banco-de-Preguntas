// Cliente REST: todas las peticiones pasan por el API Gateway.
const Api = (() => {
  async function pedir(metodo, ruta, cuerpo) {
    let respuesta;
    try {
      respuesta = await fetch(CONFIG.API + ruta, {
        method: metodo,
        headers: cuerpo ? { 'Content-Type': 'application/json' } : {},
        body: cuerpo ? JSON.stringify(cuerpo) : undefined
      });
    } catch (e) {
      throw new Error('No hay conexión con el servidor. ¿Están corriendo el gateway y los servicios?');
    }
    const texto = await respuesta.text();
    const datos = texto ? JSON.parse(texto) : null;
    if (!respuesta.ok) {
      const error = new Error((datos && (datos.detail || datos.message)) || `Error ${respuesta.status}`);
      error.errores = datos && datos.errores;
      error.estado = respuesta.status;
      throw error;
    }
    return datos;
  }

  const qs = (params) => {
    const p = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') p.append(k, v); });
    const s = p.toString();
    return s ? '?' + s : '';
  };

  return {
    // usuarios-service
    login: (login, password) => pedir('POST', '/usuarios/login', { login, password }),
    registrar: (datos) => pedir('POST', '/usuarios', datos),
    usuarios: (rol) => pedir('GET', '/usuarios' + qs({ rol })),

    // preguntas-service
    preguntas: (filtros) => pedir('GET', '/preguntas' + qs(filtros)),
    pregunta: (id) => pedir('GET', `/preguntas/${id}`),
    crearPregunta: (datos) => pedir('POST', '/preguntas', datos),
    editarPregunta: (id, datos) => pedir('PUT', `/preguntas/${id}`, datos),
    cambiarEstado: (id, estado, usuarioId) => pedir('PATCH', `/preguntas/${id}/estado`, { estado, usuarioId }),

    // revision-service
    iniciarRevision: (id, revisorId) => pedir('POST', `/revisiones/${id}/iniciar`, { revisorId }),
    evaluar: (id, revisorId, resultado, observaciones) =>
      pedir('POST', `/revisiones/${id}/evaluar`, { revisorId, resultado, observaciones }),

    // notificaciones-service
    notificaciones: (usuarioId, rol) => pedir('GET', '/notificaciones' + qs({ usuarioId, rol })),
    marcarLeida: (id) => pedir('PATCH', `/notificaciones/${id}/leida`),

    // fachada del gateway (operaciones que combinan varios servicios)
    asignacionesRevisor: (revisorId) => pedir('GET', `/fachada/revisores/${revisorId}/asignaciones`),
    detallePregunta: (id) => pedir('GET', `/fachada/preguntas/${id}/detalle`),
    asignarRevisores: (id, revisorIds) => pedir('POST', `/fachada/preguntas/${id}/revisores`, { revisorIds })
  };
})();
