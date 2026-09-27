package co.edu.unicauca.domain;

import java.util.ArrayList;
import java.util.List;

public class Question {
    public static final String STATE_BORRADOR = "Borrador";
    public static final String STATE_PENDIENTE_REVISION = "Pendiente de revisión";
    public static final String STATE_ELIMINADA = "Eliminada";

    private String id;
    private String nombre;
    private String contexto;
    private String pregunta;
    private QuestionDistractors distractors;
    private String respuestaCorrecta;
    private String justificacion;
    private String bibliografia;
    private String competencia;
    private String tema;
    private String subtema;
    private String dificultad;
    private String estado;
    private String autorLogin;
    private List<String> revisoresAsignados = new ArrayList<>();

    public Question() {
    }

    public Question(String id, String nombre, String pregunta, QuestionDistractors distractors, String respuestaCorrecta, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.pregunta = pregunta;
        this.distractors = distractors;
        this.respuestaCorrecta = respuestaCorrecta;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContexto() {
        return contexto;
    }

    public void setContexto(String contexto) {
        this.contexto = contexto;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public QuestionDistractors getDistractors() {
        return distractors;
    }

    public void setDistractors(QuestionDistractors distractors) {
        this.distractors = distractors;
    }

    public String getRespuestaCorrecta() {
        return respuestaCorrecta;
    }

    public void setRespuestaCorrecta(String respuestaCorrecta) {
        this.respuestaCorrecta = respuestaCorrecta;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public String getBibliografia() {
        return bibliografia;
    }

    public void setBibliografia(String bibliografia) {
        this.bibliografia = bibliografia;
    }

    public String getCompetencia() {
        return competencia;
    }

    public void setCompetencia(String competencia) {
        this.competencia = competencia;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getSubtema() {
        return subtema;
    }

    public void setSubtema(String subtema) {
        this.subtema = subtema;
    }

    public String getDificultad() {
        return dificultad;
    }

    public void setDificultad(String dificultad) {
        this.dificultad = dificultad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getAutorLogin() {
        return autorLogin;
    }

    public void setAutorLogin(String autorLogin) {
        this.autorLogin = autorLogin;
    }

    public List<String> getRevisoresAsignados() {
        return revisoresAsignados;
    }

    public void setRevisoresAsignados(List<String> revisoresAsignados) {
        this.revisoresAsignados = revisoresAsignados != null ? revisoresAsignados : new ArrayList<>();
    }

    @Override
    public String toString() {
        return id + " - " + nombre;
    }

    public static class Builder {
        private final Question question = new Question();

        public Builder id(String id) {
            question.id = id;
            return this;
        }

        public Builder nombre(String nombre) {
            question.nombre = nombre;
            return this;
        }

        public Builder contexto(String contexto) {
            question.contexto = contexto;
            return this;
        }

        public Builder pregunta(String pregunta) {
            question.pregunta = pregunta;
            return this;
        }

        public Builder distractors(QuestionDistractors distractors) {
            question.distractors = distractors;
            return this;
        }

        public Builder respuestaCorrecta(String respuestaCorrecta) {
            question.respuestaCorrecta = respuestaCorrecta;
            return this;
        }

        public Builder justificacion(String justificacion) {
            question.justificacion = justificacion;
            return this;
        }

        public Builder bibliografia(String bibliografia) {
            question.bibliografia = bibliografia;
            return this;
        }

        public Builder competencia(String competencia) {
            question.competencia = competencia;
            return this;
        }

        public Builder tema(String tema) {
            question.tema = tema;
            return this;
        }

        public Builder subtema(String subtema) {
            question.subtema = subtema;
            return this;
        }

        public Builder dificultad(String dificultad) {
            question.dificultad = dificultad;
            return this;
        }

        public Builder estado(String estado) {
            question.estado = estado;
            return this;
        }

        public Builder autorLogin(String autorLogin) {
            question.autorLogin = autorLogin;
            return this;
        }

        public Question build() {
            if (question.estado == null) {
                question.estado = Question.STATE_BORRADOR;
            }
            return question;
        }
    }
}
