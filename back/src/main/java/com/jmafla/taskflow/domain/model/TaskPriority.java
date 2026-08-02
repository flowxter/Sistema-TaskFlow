package com.jmafla.taskflow.domain.model;

/**
 * Prioridad de una tarea.
 *
 * <p>El campo {@code weight} permite ordenar y calcular metricas sin depender
 * del orden de declaracion del enum, que es fragil ante refactorizaciones.</p>
 */
public enum TaskPriority {

    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int weight;

    TaskPriority(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    /** Indica si la prioridad exige atencion inmediata del equipo. */
    public boolean isUrgent() {
        return this.weight >= HIGH.weight;
    }
}
