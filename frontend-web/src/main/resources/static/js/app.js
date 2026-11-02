// Cliente web del Banco de Preguntas. Sin frameworks: HTML + JavaScript que consume las APIs REST.
(() => {
  const $ = (sel, raiz = document) => raiz.querySelector(sel);
  const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const fecha = (iso) => iso ? new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' }) : '';

  const ESTADOS = {
    BORRADOR: 'Borrador', PENDIENTE_REVISION: 'Pendiente de revisión', EN_REVISION: 'En revisión',
    APROBADA: 'Aprobada', RECHAZADA: 'Rechazada', PUBLICADA: 'Publicada', ARCHIVADA: 'Archivada'
  };
  const ESTADOS_REVISION = { ASIGNADA: 'Asignada', EN_REVISION: 'En revisión', APROBADA: 'Aprobada', RECHAZADA: 'Rechazada' };
  const ROLES = { ADMINISTRADOR: 'Administrador', AUTOR: 'Autor', REVISOR: 'Revisor', DOCENTE: 'Docente', ESTUDIANTE: 'Estudiante' };

  const VISTAS = {
    AUTOR: [['mis-preguntas', 'Mis preguntas'], ['nueva', 'Nueva pregunta']],
    ADMINISTRADOR: [['banco', 'Banco de preguntas'], ['pendientes', 'Pendientes de revisión']],
    REVISOR: [['mis-revisiones', 'Mis revisiones']],
    DOCENTE: [['publicadas', 'Banco publicado']],
    ESTUDIANTE: [['publicadas', 'Banco publicado']]
  };

  let usuario = JSON.parse(sessionStorage.getItem('usuario') || 'null');
  let vistaActual = null;
  let pagina = 0;
  let filtros = {};

  // ------------------------------------------------------------------ utilidades de interfaz

  function aviso(texto, tipo = 'ok') {
    const div = document.createElement('div');
    div.className = `aviso ${tipo}`;
    div.textContent = texto;
    $('#avisos').appendChild(div);
    setTimeout(() => div.remove(), 4500);
  }

  function errorDe(e) {
    const extra = e.errores ? '\n• ' + e.errores.join('\n• ') : '';
    aviso(e.message + extra, 'error');
  }

  function abrirDialogo(html) {
    $('#dialogo-contenido').innerHTML = html + '<div class="acciones-dialogo"><button class="enlace" data-cerrar>Cerrar</button></div>';
    $('#dialogo').showModal();
  }
  function cerrarDialogo() { $('#dialogo').close(); }
  $('#dialogo').addEventListener('click', (ev) => {
    if (ev.target.matches('[data-cerrar]') || ev.target === $('#dialogo')) cerrarDialogo();
  });

  const chip = (estado, mapa = ESTADOS) => `<span class="chip estado-${esc(estado)}">${esc(mapa[estado] || estado)}</span>`;

  // ------------------------------------------------------------------ acceso

  $('#ir-registro').onclick = (e) => { e.preventDefault(); $('#form-login').classList.add('oculto'); $('#form-registro').classList.remove('oculto'); };
  $('#ir-login').onclick = (e) => { e.preventDefault(); $('#form-registro').classList.add('oculto'); $('#form-login').classList.remove('oculto'); };

  $('#form-login').onsubmit = async (e) => {
    e.preventDefault();
    const f = new FormData(e.target);
    try {
      iniciarSesion(await Api.login(f.get('login'), f.get('password')));
    } catch (err) { errorDe(err); }
  };

  $('#form-registro').onsubmit = async (e) => {
    e.preventDefault();
    try {
      await Api.registrar(Object.fromEntries(new FormData(e.target)));
      aviso('Cuenta creada. Ya puede ingresar.');
      e.target.reset();
      $('#ir-login').click();
    } catch (err) { errorDe(err); }
  };

  $('#btn-salir').onclick = () => { sessionStorage.removeItem('usuario'); location.reload(); };

  function iniciarSesion(u) {
    usuario = u;
    sessionStorage.setItem('usuario', JSON.stringify(u));
    $('#vista-acceso').classList.add('oculto');
    $('#vista-app').classList.remove('oculto');
    $('#nombre-usuario').innerHTML = `${esc(u.nombreCompleto)} <small>${esc(ROLES[u.rol])}</small>`;
    const vistas = VISTAS[u.rol] || [];
    $('#menu').innerHTML = vistas.map(([id, t]) => `<button data-vista="${id}">${t}</button>`).join('') +
      '<button data-vista="notificaciones">Notificaciones</button>';
    $('#menu').onclick = (ev) => { const b = ev.target.closest('[data-vista]'); if (b) mostrar(b.dataset.vista); };
    mostrar(vistas.length ? vistas[0][0] : 'notificaciones');
    actualizarContador();
    setInterval(actualizarContador, 15000);
  }

  function mostrar(vista) {
    vistaActual = vista;
    pagina = 0;
    filtros = {};
    document.querySelectorAll('#menu button').forEach((b) => b.classList.toggle('activo', b.dataset.vista === vista));
    ({
      'mis-preguntas': vistaListado,
      banco: vistaListado,
      pendientes: vistaListado,
      publicadas: vistaListado,
      nueva: () => vistaFormulario(),
      'mis-revisiones': vistaRevisiones,
      notificaciones: vistaNotificaciones
    }[vista])();
  }

  // ------------------------------------------------------------------ listado con filtros y paginacion (HU-3)

  function filtrosBase() {
    switch (vistaActual) {
      case 'mis-preguntas': return { autorId: usuario.id };
      case 'pendientes': return { estado: 'PENDIENTE_REVISION' };
      case 'publicadas': return { estado: 'PUBLICADA' };
      default: return {};
    }
  }

  function vistaListado() {
    const titulos = { 'mis-preguntas': 'Mis preguntas', banco: 'Banco de preguntas', pendientes: 'Pendientes de revisión', publicadas: 'Banco publicado' };
    const conEstado = vistaActual === 'mis-preguntas' || vistaActual === 'banco';
    $('#contenido').innerHTML = `
      <h2>${titulos[vistaActual]}</h2>
      <form id="filtros" class="filtros tarjeta">
        ${conEstado ? `<label>Estado <select name="estado"><option value="">Todos</option>
          ${Object.entries(ESTADOS).map(([k, v]) => `<option value="${k}">${v}</option>`).join('')}</select></label>` : ''}
        <label>Tema <input name="tema" placeholder="Ej. Estadística"></label>
        <label>Competencia <input name="competencia" placeholder="Ej. Lectura crítica"></label>
        <label>Dificultad <select name="dificultad"><option value="">Todas</option>
          <option>BAJA</option><option>MEDIA</option><option>ALTA</option></select></label>
        <label class="ancho">Buscar <input name="texto" placeholder="Título, enunciado o tema"></label>
        <button class="primario">Filtrar</button>
      </form>
      <div id="resultados"></div>`;
    $('#filtros').onsubmit = (e) => {
      e.preventDefault();
      filtros = Object.fromEntries(new FormData(e.target));
      pagina = 0;
      cargarListado();
    };
    cargarListado();
  }

  async function cargarListado() {
    const caja = $('#resultados');
    caja.innerHTML = '<p class="sutil">Cargando…</p>';
    try {
      const datos = await Api.preguntas({ ...filtros, ...filtrosBase(), pagina, tamano: CONFIG.TAMANO_PAGINA });
      if (!datos.contenido.length) {
        caja.innerHTML = '<p class="vacio">No hay preguntas con esos filtros.</p>';
        return;
      }
      caja.innerHTML = `
        <table class="tabla">
          <thead><tr><th>#</th><th>Título</th><th>Competencia / tema</th><th>Dificultad</th><th>Estado</th><th>Actualizada</th><th></th></tr></thead>
          <tbody>${datos.contenido.map(filaPregunta).join('')}</tbody>
        </table>
        <div class="paginacion">
          <button data-pag="-1" ${datos.pagina === 0 ? 'disabled' : ''}>‹ Anterior</button>
          <span>Página ${datos.pagina + 1} de ${Math.max(datos.totalPaginas, 1)} · ${datos.totalElementos} preguntas</span>
          <button data-pag="1" ${datos.pagina + 1 >= datos.totalPaginas ? 'disabled' : ''}>Siguiente ›</button>
        </div>`;
      caja.querySelector('.paginacion').onclick = (ev) => {
        const b = ev.target.closest('[data-pag]');
        if (b && !b.disabled) { pagina += Number(b.dataset.pag); cargarListado(); }
      };
      caja.querySelector('tbody').onclick = (ev) => {
        const b = ev.target.closest('[data-accion]');
        if (b) accionPregunta(b.dataset.accion, Number(b.dataset.id));
      };
    } catch (e) {
      caja.innerHTML = '';
      errorDe(e);
    }
  }

  function filaPregunta(p) {
    return `<tr>
      <td>${p.id}</td>
      <td><strong>${esc(p.titulo)}</strong></td>
      <td>${esc(p.competencia)}<br><small class="sutil">${esc(p.tema)} · ${esc(p.subtema)}</small></td>
      <td>${esc(p.dificultad)}</td>
      <td>${chip(p.estado)}</td>
      <td><small>${fecha(p.fechaActualizacion)}</small></td>
      <td class="acciones">${botonesPregunta(p)}</td>
    </tr>`;
  }

  function botonesPregunta(p) {
    const b = (accion, texto, clase = '') => `<button class="mini ${clase}" data-accion="${accion}" data-id="${p.id}">${texto}</button>`;
    const botones = [b('ver', 'Ver')];
    const esAutor = usuario.rol === 'AUTOR' && p.autorId === usuario.id;
    const esAdmin = usuario.rol === 'ADMINISTRADOR';
    if (esAutor && p.estado === 'BORRADOR') botones.push(b('editar', 'Editar'), b('enviar', 'Enviar a revisión', 'primario'));
    if (esAutor && p.estado === 'RECHAZADA') botones.push(b('corregir', 'Corregir'));
    if (esAdmin && p.estado === 'PENDIENTE_REVISION') botones.push(b('asignar', 'Asignar revisores', 'primario'));
    if (esAdmin && p.estado === 'APROBADA') botones.push(b('publicar', 'Publicar', 'primario'));
    if ((esAdmin || esAutor) && ['BORRADOR', 'APROBADA', 'RECHAZADA', 'PUBLICADA'].includes(p.estado)) botones.push(b('archivar', 'Archivar', 'peligro'));
    return botones.join('');
  }

  async function accionPregunta(accion, id) {
    try {
      switch (accion) {
        case 'ver': return verPregunta(id);
        case 'editar': return vistaFormulario(await Api.pregunta(id));
        case 'enviar':
          await Api.cambiarEstado(id, 'PENDIENTE_REVISION', usuario.id);
          aviso('Pregunta enviada a revisión. Se notificó al administrador.');
          break;
        case 'corregir':
          await Api.cambiarEstado(id, 'BORRADOR', usuario.id);
          aviso('La pregunta volvió a Borrador para que la corrija.');
          break;
        case 'publicar':
          await Api.cambiarEstado(id, 'PUBLICADA', usuario.id);
          aviso('Pregunta publicada en el banco.');
          break;
        case 'archivar':
          await Api.cambiarEstado(id, 'ARCHIVADA', usuario.id);
          aviso('Pregunta archivada.');
          break;
        case 'asignar': return dialogoAsignar(id);
      }
      cargarListado();
    } catch (e) { errorDe(e); }
  }

  // ------------------------------------------------------------------ detalle

  function htmlPregunta(p, { mostrarClave = true } = {}) {
    const opciones = ['A', 'B', 'C', 'D'].map((l) => {
      const correcta = mostrarClave && p.respuestaCorrecta === l;
      return `<li class="${correcta ? 'correcta' : ''}"><b>${l}.</b> ${esc(p['opcion' + l])}${correcta ? ' <span class="chip estado-APROBADA">Correcta</span>' : ''}</li>`;
    }).join('');
    return `
      <div class="pregunta">
        <div class="cabecera-pregunta"><h3>${esc(p.titulo)}</h3>${chip(p.estado)}</div>
        <p class="sutil">${esc(p.competencia)} · ${esc(p.tema)} · ${esc(p.subtema)} · Dificultad ${esc(p.dificultad)}</p>
        <h4>Contexto</h4><p class="texto">${esc(p.contexto)}</p>
        <h4>Pregunta</h4><p class="texto"><strong>${esc(p.enunciado)}</strong></p>
        <ol class="opciones">${opciones}</ol>
        ${mostrarClave ? `<h4>Justificación</h4><p class="texto">${esc(p.justificacion)}</p>` : ''}
        <h4>Bibliografía</h4><p class="texto">${esc(p.bibliografia)}</p>
      </div>`;
  }

  async function verPregunta(id) {
    try {
      const { pregunta, revisiones } = await Api.detallePregunta(id);
      const mostrarClave = usuario.rol !== 'ESTUDIANTE';
      const historial = revisiones.length ? `
        <h4>Historial de revisiones</h4>
        <ul class="historial">${revisiones.map((r) => `
          <li>${chip(r.estado, ESTADOS_REVISION)} <b>${esc(r.revisorNombre)}</b>
            <small class="sutil">asignada ${fecha(r.fechaAsignacion)}${r.fechaEvaluacion ? ' · evaluada ' + fecha(r.fechaEvaluacion) : ''}</small>
            ${r.observaciones ? `<p class="observacion">${esc(r.observaciones)}</p>` : ''}</li>`).join('')}
        </ul>` : '';
      abrirDialogo(htmlPregunta(pregunta, { mostrarClave }) + historial);
    } catch (e) { errorDe(e); }
  }

  // ------------------------------------------------------------------ crear / editar (HU-1, RF-06)

  function vistaFormulario(p = null) {
    const v = (campo) => esc(p ? p[campo] : '');
    $('#contenido').innerHTML = `
      <h2>${p ? 'Editar pregunta #' + p.id : 'Nueva pregunta'}</h2>
      <form id="form-pregunta" class="tarjeta formulario dos-columnas">
        <label class="ancho">Título <input name="titulo" value="${v('titulo')}" required></label>
        <label>Competencia <input name="competencia" value="${v('competencia')}" required placeholder="Ej. Razonamiento cuantitativo"></label>
        <label>Tema <input name="tema" value="${v('tema')}" required></label>
        <label>Subtema <input name="subtema" value="${v('subtema')}" required></label>
        <label>Dificultad <select name="dificultad">
          ${['BAJA', 'MEDIA', 'ALTA'].map((d) => `<option ${p && p.dificultad === d ? 'selected' : ''}>${d}</option>`).join('')}</select></label>
        <label class="ancho">Contexto <textarea name="contexto" rows="4" required>${v('contexto')}</textarea></label>
        <label class="ancho">Pregunta directa (una sola) <input name="enunciado" value="${v('enunciado')}" required></label>
        ${['A', 'B', 'C', 'D'].map((l) => `<label>Opción ${l} <input name="opcion${l}" value="${v('opcion' + l)}" required></label>`).join('')}
        <label>Respuesta correcta <select name="respuestaCorrecta">
          ${['A', 'B', 'C', 'D'].map((l) => `<option ${p && p.respuestaCorrecta === l ? 'selected' : ''}>${l}</option>`).join('')}</select></label>
        <label class="ancho">Justificación de la respuesta <textarea name="justificacion" rows="3" required>${v('justificacion')}</textarea></label>
        <label class="ancho">Bibliografía <input name="bibliografia" value="${v('bibliografia')}" required></label>
        <p class="sutil ancho">No use opciones como "Todas las anteriores" ni "Ninguna de las anteriores".</p>
        <div class="ancho fila-botones">
          <button type="submit" class="primario">${p ? 'Guardar cambios' : 'Crear en borrador'}</button>
        </div>
      </form>`;
    $('#form-pregunta').onsubmit = async (e) => {
      e.preventDefault();
      const datos = { ...Object.fromEntries(new FormData(e.target)), usuarioId: usuario.id };
      try {
        if (p) await Api.editarPregunta(p.id, datos);
        else await Api.crearPregunta(datos);
        aviso(p ? 'Cambios guardados.' : 'Pregunta creada en Borrador.');
        mostrar('mis-preguntas');
      } catch (err) { errorDe(err); }
    };
  }

  // ------------------------------------------------------------------ asignar revisores (HU-4, via fachada)

  async function dialogoAsignar(id) {
    try {
      const [pregunta, revisores] = await Promise.all([Api.pregunta(id), Api.usuarios('REVISOR')]);
      const activos = revisores.filter((r) => r.estado === 'ACTIVO');
      abrirDialogo(`
        <h3>Asignar revisores</h3>
        <p>${esc(pregunta.titulo)}</p>
        <form id="form-asignar" class="formulario">
          ${activos.length ? activos.map((r) => `<label class="check"><input type="checkbox" name="revisor" value="${r.id}"> ${esc(r.nombreCompleto)} <small class="sutil">(${esc(r.login)})</small></label>`).join('')
            : '<p class="vacio">No hay revisores registrados.</p>'}
          <button class="primario" ${activos.length ? '' : 'disabled'}>Asignar</button>
        </form>`);
      $('#form-asignar').onsubmit = async (e) => {
        e.preventDefault();
        const ids = [...e.target.querySelectorAll('input[name=revisor]:checked')].map((c) => Number(c.value));
        try {
          await Api.asignarRevisores(id, ids);
          cerrarDialogo();
          aviso('Revisores asignados. Cada uno recibirá una notificación.');
          setTimeout(cargarListado, 600);
        } catch (err) { errorDe(err); }
      };
    } catch (e) { errorDe(e); }
  }

  // ------------------------------------------------------------------ vista del revisor (HU-5, T5.4)

  async function vistaRevisiones() {
    $('#contenido').innerHTML = '<h2>Mis revisiones</h2><div id="resultados"><p class="sutil">Cargando…</p></div>';
    try {
      const items = await Api.asignacionesRevisor(usuario.id);
      const caja = $('#resultados');
      if (!items.length) {
        caja.innerHTML = '<p class="vacio">No tiene preguntas asignadas.</p>';
        return;
      }
      caja.innerHTML = `<div class="lista-revisiones">${items.map(({ revision: r, pregunta: p }) => `
        <article class="tarjeta revision">
          <div class="cabecera-pregunta"><h3>${esc(p ? p.titulo : 'Pregunta ' + r.preguntaId)}</h3>${chip(r.estado, ESTADOS_REVISION)}</div>
          <p class="sutil">${p ? esc(p.competencia) + ' · ' + esc(p.tema) : ''} · asignada ${fecha(r.fechaAsignacion)}</p>
          ${r.observaciones ? `<p class="observacion">${esc(r.observaciones)}</p>` : ''}
          <button class="mini ${['ASIGNADA', 'EN_REVISION'].includes(r.estado) ? 'primario' : ''}" data-revision="${r.id}">
            ${['ASIGNADA', 'EN_REVISION'].includes(r.estado) ? 'Leer y evaluar' : 'Ver'}</button>
        </article>`).join('')}</div>`;
      caja.onclick = (ev) => {
        const b = ev.target.closest('[data-revision]');
        if (b) abrirRevision(items.find((i) => i.revision.id === Number(b.dataset.revision)));
      };
    } catch (e) { errorDe(e); }
  }

  async function abrirRevision({ revision, pregunta }) {
    let r = revision;
    const pendiente = ['ASIGNADA', 'EN_REVISION'].includes(r.estado);
    if (r.estado === 'ASIGNADA') {
      try { r = await Api.iniciarRevision(r.id, usuario.id); } catch (e) { errorDe(e); }
    }
    abrirDialogo(`
      ${pregunta ? htmlPregunta(pregunta) : '<p class="vacio">La pregunta ya no está disponible.</p>'}
      ${pendiente ? `
        <form id="form-evaluar" class="formulario evaluacion">
          <h4>Formato de evaluación</h4>
          <label>Observaciones para el autor (obligatorias si rechaza)
            <textarea name="observaciones" rows="4" maxlength="1000"></textarea></label>
          <div class="fila-botones">
            <button type="submit" name="resultado" value="APROBADA" class="primario">Aprobar</button>
            <button type="submit" name="resultado" value="RECHAZADA" class="peligro">Rechazar</button>
          </div>
        </form>` : `<p>Su dictamen: ${chip(r.estado, ESTADOS_REVISION)}</p>`}`);
    const form = $('#form-evaluar');
    if (form) {
      form.onsubmit = async (e) => {
        e.preventDefault();
        const resultado = e.submitter.value;
        try {
          await Api.evaluar(r.id, usuario.id, resultado, new FormData(form).get('observaciones'));
          cerrarDialogo();
          aviso(resultado === 'APROBADA' ? 'Pregunta aprobada.' : 'Pregunta rechazada con observaciones.');
          vistaRevisiones();
        } catch (err) { errorDe(err); }
      };
    }
  }

  // ------------------------------------------------------------------ notificaciones

  async function actualizarContador() {
    if (!usuario) return;
    try {
      const lista = await Api.notificaciones(usuario.id, usuario.rol);
      const sinLeer = lista.filter((n) => !n.leida).length;
      const c = $('#contador');
      c.textContent = sinLeer;
      c.classList.toggle('oculto', sinLeer === 0);
    } catch (e) { /* el contador es informativo */ }
  }
  $('#btn-notificaciones').onclick = () => mostrar('notificaciones');

  async function vistaNotificaciones() {
    $('#contenido').innerHTML = '<h2>Notificaciones</h2><div id="resultados"><p class="sutil">Cargando…</p></div>';
    try {
      const lista = await Api.notificaciones(usuario.id, usuario.rol);
      const caja = $('#resultados');
      caja.innerHTML = lista.length ? `<ul class="notificaciones">${lista.map((n) => `
        <li class="tarjeta ${n.leida ? 'leida' : ''}">
          <div><strong>${esc(n.titulo)}</strong> <small class="sutil">${fecha(n.fecha)}</small></div>
          <p>${esc(n.mensaje)}</p>
          ${n.leida ? '' : `<button class="mini" data-leer="${n.id}">Marcar como leída</button>`}
        </li>`).join('')}</ul>` : '<p class="vacio">No tiene notificaciones.</p>';
      caja.onclick = async (ev) => {
        const b = ev.target.closest('[data-leer]');
        if (!b) return;
        try { await Api.marcarLeida(b.dataset.leer); vistaNotificaciones(); actualizarContador(); } catch (e) { errorDe(e); }
      };
    } catch (e) { errorDe(e); }
  }

  // ------------------------------------------------------------------ inicio
  if (usuario) iniciarSesion(usuario);
})();
