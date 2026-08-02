package com.jmafla.taskflow.domain.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Estados de una tarea y las transiciones validas entre ellos.
 *
 * <p><b>Patron State:</b> cada constante conoce a que estados puede moverse.
 * Concentrar la regla aqui evita el clasico {@code if/else} disperso por los
 * servicios y hace que agregar un estado nuevo sea un cambio localizado.</p>
 *
 * <pre>
 *   BACKLOG -> IN_PROGRESS -> IN_REVIEW -> DONE
 *                  |              |
 *                  +--> BLOCKED <-+
 * </pre>
 */
public enum TaskStatus {

    /** Pendiente de iniciar. */
    BACKLOG,

    /** En desarrollo activo. */
    IN_PROGRESS,

    /** En revision de pares o QA. */
    IN_REVIEW,

    /** Detenida por una dependencia externa. */
    BLOCKED,

    /** Terminada y aceptada. Estado final. */
    DONE;

    private Set<TaskStatus> allowedTransitions;

    static {
        // La inicializacion va en un bloque estatico porque un enum no puede
        // referenciar a sus pares dentro del propio constructor.
        BACKLOG.allowedTransitions = EnumSet.of(IN_PROGRESS, BLOCKED);
        IN_PROGRESS.allowedTransitions = EnumSet.of(IN_REVIEW, BLOCKED, BACKLOG);
        IN_REVIEW.allowedTransitions = EnumSet.of(DONE, IN_PROGRESS, BLOCKED);
        BLOCKED.allowedTransitions = EnumSet.of(BACKLOG, IN_PROGRESS);
        DONE.allowedTransitions = EnumSet.noneOf(TaskStatus.class);
    }

    /**
     * Verifica si la tarea puede pasar de este estado al indicado.
     *
     * @param target estado destino
     * @return {@code true} si la transicion esta permitida
     */
    public boolean canTransitionTo(TaskStatus target) {
        return allowedTransitions.contains(target);
    }

    /** Estados alcanzables desde el actual. Util para que el cliente pinte solo botones validos. */
    public Set<TaskStatus> getAllowedTransitions() {
        return Collections.unmodifiableSet(allowedTransitions);
    }

    /** Indica si el estado cierra el ciclo de vida de la tarea. */
    public boolean isFinal() {
        return this == DONE;
    }
}
