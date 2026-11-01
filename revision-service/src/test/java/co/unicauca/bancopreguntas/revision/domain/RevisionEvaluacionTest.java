package co.unicauca.bancopreguntas.revision.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RevisionEvaluacionTest {

    private static final Long PREGUNTA = 10L;
    private static final Long REVISOR = 7L;

    private Revision revision;

    @BeforeEach
    void setUp() {
        revision = new Revision(PREGUNTA, REVISOR);
    }

    @Test
    void aprobarSinObservacionesDejaLaRevisionAprobada() {
        revision.evaluar(REVISOR, EstadoRevision.APROBADA, null);

        assertThat(revision.getEstado()).isEqualTo(EstadoRevision.APROBADA);
        assertThat(revision.getObservaciones()).isNull();
        assertThat(revision.getFechaEvaluacion()).isNotNull();
    }

    @Test
    void aprobarConObservacionesLasGuardaSinEspaciosSobrantes() {
        revision.evaluar(REVISOR, EstadoRevision.APROBADA, "  Bien redactada  ");

        assertThat(revision.getObservaciones()).isEqualTo("Bien redactada");
    }

    @Test
    void rechazarConObservacionesDejaLaRevisionRechazada() {
        revision.evaluar(REVISOR, EstadoRevision.RECHAZADA, "La opcion C tambien es correcta");

        assertThat(revision.getEstado()).isEqualTo(EstadoRevision.RECHAZADA);
        assertThat(revision.getObservaciones()).isEqualTo("La opcion C tambien es correcta");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void rechazarSinObservacionesNoSePermite(String observaciones) {
        assertThatThrownBy(() -> revision.evaluar(REVISOR, EstadoRevision.RECHAZADA, observaciones))
                .isInstanceOf(ReglaRevisionException.class)
                .hasMessageContaining("observaciones");
        assertThat(revision.getEstado()).isEqualTo(EstadoRevision.ASIGNADA);
    }

    @Test
    void observacionesDemasiadoLargasNoSePermiten() {
        String larga = "x".repeat(Revision.MAX_OBSERVACIONES + 1);

        assertThatThrownBy(() -> revision.evaluar(REVISOR, EstadoRevision.RECHAZADA, larga))
                .isInstanceOf(ReglaRevisionException.class);
    }

    @Test
    void soloElRevisorAsignadoPuedeEvaluar() {
        assertThatThrownBy(() -> revision.evaluar(99L, EstadoRevision.APROBADA, null))
                .isInstanceOf(ReglaRevisionException.class)
                .hasMessageContaining("revisor asignado");
        assertThatThrownBy(() -> revision.evaluar(null, EstadoRevision.APROBADA, null))
                .isInstanceOf(ReglaRevisionException.class);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoRevision.class, names = {"ASIGNADA", "EN_REVISION"})
    void elResultadoDebeSerAprobadaORechazada(EstadoRevision resultado) {
        assertThatThrownBy(() -> revision.evaluar(REVISOR, resultado, "texto"))
                .isInstanceOf(ReglaRevisionException.class);
    }

    @Test
    void elResultadoNoPuedeSerNulo() {
        assertThatThrownBy(() -> revision.evaluar(REVISOR, null, "texto"))
                .isInstanceOf(ReglaRevisionException.class);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoRevision.class, names = {"APROBADA", "RECHAZADA"})
    void unaRevisionEvaluadaNoSePuedeVolverAEvaluar(EstadoRevision segundoResultado) {
        revision.evaluar(REVISOR, EstadoRevision.APROBADA, null);

        assertThatThrownBy(() -> revision.evaluar(REVISOR, segundoResultado, "otra vez"))
                .isInstanceOf(ReglaRevisionException.class);
        assertThat(revision.getEstado()).isEqualTo(EstadoRevision.APROBADA);
    }

    @Test
    void reglasDeTransicionDeEstado() {
        assertThat(EstadoRevision.ASIGNADA.puedeTransicionarA(EstadoRevision.EN_REVISION)).isTrue();
        assertThat(EstadoRevision.ASIGNADA.puedeTransicionarA(EstadoRevision.APROBADA)).isTrue();
        assertThat(EstadoRevision.ASIGNADA.puedeTransicionarA(EstadoRevision.RECHAZADA)).isTrue();
        assertThat(EstadoRevision.EN_REVISION.puedeTransicionarA(EstadoRevision.APROBADA)).isTrue();
        assertThat(EstadoRevision.EN_REVISION.puedeTransicionarA(EstadoRevision.RECHAZADA)).isTrue();
        assertThat(EstadoRevision.EN_REVISION.puedeTransicionarA(EstadoRevision.ASIGNADA)).isFalse();
        for (EstadoRevision destino : EstadoRevision.values()) {
            assertThat(EstadoRevision.APROBADA.puedeTransicionarA(destino)).isFalse();
            assertThat(EstadoRevision.RECHAZADA.puedeTransicionarA(destino)).isFalse();
        }
    }
}
