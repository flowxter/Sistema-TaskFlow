package com.jmafla.taskflow.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de la maquina de estados.
 *
 * <p>Es la regla de negocio mas critica del sistema, asi que se prueba de forma
 * aislada, sin base de datos ni contexto de Spring. Corren en milisegundos.</p>
 */
@DisplayName("Maquina de estados de una tarea")
class TaskStatusTest {

    @Nested
    @DisplayName("Transiciones permitidas")
    class AllowedTransitions {

        @ParameterizedTest(name = "{0} puede pasar a {1}")
        @CsvSource({
                "BACKLOG,     IN_PROGRESS",
                "BACKLOG,     BLOCKED",
                "IN_PROGRESS, IN_REVIEW",
                "IN_PROGRESS, BLOCKED",
                "IN_PROGRESS, BACKLOG",
                "IN_REVIEW,   DONE",
                "IN_REVIEW,   IN_PROGRESS",
                "BLOCKED,     IN_PROGRESS"
        })
        void shouldAllowValidTransitions(TaskStatus from, TaskStatus to) {
            assertThat(from.canTransitionTo(to)).isTrue();
        }
    }

    @Nested
    @DisplayName("Transiciones rechazadas")
    class RejectedTransitions {

        @ParameterizedTest(name = "{0} no puede saltar a {1}")
        @CsvSource({
                "BACKLOG,     DONE",
                "BACKLOG,     IN_REVIEW",
                "IN_PROGRESS, DONE",
                "BLOCKED,     DONE",
                "BLOCKED,     IN_REVIEW"
        })
        void shouldRejectInvalidTransitions(TaskStatus from, TaskStatus to) {
            assertThat(from.canTransitionTo(to)).isFalse();
        }

        @ParameterizedTest(name = "DONE no puede volver a {0}")
        @EnumSource(TaskStatus.class)
        void shouldNotAllowAnyTransitionFromDone(TaskStatus target) {
            assertThat(TaskStatus.DONE.canTransitionTo(target)).isFalse();
        }
    }

    @Test
    @DisplayName("DONE es el unico estado final")
    void shouldIdentifyDoneAsTheOnlyFinalStatus() {
        assertThat(TaskStatus.DONE.isFinal()).isTrue();

        assertThat(TaskStatus.BACKLOG.isFinal()).isFalse();
        assertThat(TaskStatus.IN_PROGRESS.isFinal()).isFalse();
        assertThat(TaskStatus.IN_REVIEW.isFinal()).isFalse();
        assertThat(TaskStatus.BLOCKED.isFinal()).isFalse();
    }

    @Test
    @DisplayName("El conjunto de transiciones expuesto es inmutable")
    void shouldExposeTransitionsAsReadOnly() {
        // Protege la maquina de estados de modificaciones accidentales desde fuera.
        assertThat(TaskStatus.BACKLOG.getAllowedTransitions())
                .isUnmodifiable()
                .containsExactlyInAnyOrder(TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED);
    }
}
