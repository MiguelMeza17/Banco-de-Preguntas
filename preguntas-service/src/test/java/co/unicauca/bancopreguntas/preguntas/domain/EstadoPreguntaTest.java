package co.unicauca.bancopreguntas.preguntas.domain;

import co.unicauca.bancopreguntas.preguntas.PreguntasDePrueba;
import co.unicauca.bancopreguntas.preguntas.domain.estado.CodigoEstado;
import co.unicauca.bancopreguntas.preguntas.domain.estado.TransicionInvalidaException;
import org.junit.jupiter.api.Test;

import static co.unicauca.bancopreguntas.preguntas.PreguntasDePrueba.AUTOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Patron State: transiciones validas e invalidas del ciclo de vida (RF-14, RF-15). */
class EstadoPreguntaTest {

    private Pregunta enEstado(CodigoEstado estado) {
        Pregunta p = PreguntasDePrueba.pregunta();
        switch (estado) {
            case BORRADOR -> { }
            case PENDIENTE_REVISION -> p.enviarARevision(AUTOR);
            case EN_REVISION -> { p.enviarARevision(AUTOR); p.iniciarRevision(); }
            case APROBADA -> { p.enviarARevision(AUTOR); p.iniciarRevision(); p.aprobar(); }
            case RECHAZADA -> { p.enviarARevision(AUTOR); p.iniciarRevision(); p.rechazar(); }
            case PUBLICADA -> { p.enviarARevision(AUTOR); p.iniciarRevision(); p.aprobar(); p.publicar(); }
            case ARCHIVADA -> p.archivar();
        }
        assertThat(p.getEstado()).isEqualTo(estado);
        return p;
    }

    @Test
    void unaPreguntaNuevaNaceEnBorradorYEsEditable() {
        Pregunta p = PreguntasDePrueba.pregunta();

        assertThat(p.getEstado()).isEqualTo(CodigoEstado.BORRADOR);
        assertThat(p.comportamiento().permiteEdicion()).isTrue();
    }

    @Test
    void flujoCompletoHastaPublicarYArchivar() {
        Pregunta p = enEstado(CodigoEstado.PUBLICADA);

        p.archivar();

        assertThat(p.getEstado()).isEqualTo(CodigoEstado.ARCHIVADA);
    }

    @Test
    void unaRechazadaVuelveABorradorParaCorregirse() {
        Pregunta p = enEstado(CodigoEstado.RECHAZADA);

        p.devolverABorrador(AUTOR);

        assertThat(p.getEstado()).isEqualTo(CodigoEstado.BORRADOR);
        assertThat(p.comportamiento().permiteEdicion()).isTrue();
    }

    @Test
    void soloElAutorPuedeEnviarARevision() {
        Pregunta p = PreguntasDePrueba.pregunta();

        assertThatThrownBy(() -> p.enviarARevision(99L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("autor");
        assertThat(p.getEstado()).isEqualTo(CodigoEstado.BORRADOR);
    }

    @Test
    void noSePuedeAprobarSinPasarPorRevision() {
        assertThatThrownBy(() -> enEstado(CodigoEstado.BORRADOR).aprobar())
                .isInstanceOf(TransicionInvalidaException.class);
        assertThatThrownBy(() -> enEstado(CodigoEstado.PENDIENTE_REVISION).aprobar())
                .isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void noSePuedePublicarUnaPreguntaSinAprobar() {
        for (CodigoEstado estado : new CodigoEstado[]{CodigoEstado.BORRADOR, CodigoEstado.PENDIENTE_REVISION,
                CodigoEstado.EN_REVISION, CodigoEstado.RECHAZADA}) {
            assertThatThrownBy(() -> enEstado(estado).publicar())
                    .as("publicar desde %s", estado)
                    .isInstanceOf(TransicionInvalidaException.class);
        }
    }

    @Test
    void noSePuedeEnviarDosVecesARevision() {
        Pregunta p = enEstado(CodigoEstado.PENDIENTE_REVISION);

        assertThatThrownBy(() -> p.enviarARevision(AUTOR))
                .isInstanceOf(TransicionInvalidaException.class)
                .hasMessageContaining("Pendiente de revision");
    }

    @Test
    void archivadaEsUnEstadoFinal() {
        Pregunta p = enEstado(CodigoEstado.ARCHIVADA);

        assertThatThrownBy(p::publicar).isInstanceOf(TransicionInvalidaException.class);
        assertThatThrownBy(() -> p.enviarARevision(AUTOR)).isInstanceOf(TransicionInvalidaException.class);
        assertThatThrownBy(p::archivar).isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    void soloSeEditaEnBorrador() {
        Pregunta cambios = PreguntasDePrueba.pregunta();
        Pregunta pendiente = enEstado(CodigoEstado.PENDIENTE_REVISION);

        assertThatThrownBy(() -> pendiente.actualizarContenido(cambios, AUTOR))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Borrador");
    }

    @Test
    void editarEnBorradorReemplazaElContenido() {
        Pregunta p = PreguntasDePrueba.pregunta();
        Pregunta cambios = PreguntasDePrueba.pregunta(PreguntasDePrueba.conTemaYCompetencia(
                "Algebra", "Razonamiento cuantitativo", "ALTA", "Nuevo titulo"));

        p.actualizarContenido(cambios, AUTOR);

        assertThat(p.getTitulo()).isEqualTo("Nuevo titulo");
        assertThat(p.getTema()).isEqualTo("Algebra");
        assertThat(p.getDificultad()).isEqualTo(Dificultad.ALTA);
        assertThat(p.getEstado()).isEqualTo(CodigoEstado.BORRADOR);
    }
}
