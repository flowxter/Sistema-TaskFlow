package com.jmafla.taskflow.web.dto.response;

import com.jmafla.taskflow.domain.model.TaskPriority;
import com.jmafla.taskflow.domain.model.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

/**
 * Representacion publica de una tarea.
 *
 * <p>Incluye {@code allowedTransitions} para que el cliente pueda habilitar
 * unicamente las acciones validas sin duplicar la maquina de estados en el
 * frontend. La regla vive en un solo lugar: el dominio.</p>
 */
public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        String assignee,
        LocalDate dueDate,
        boolean overdue,
        Set<TaskStatus> allowedTransitions,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
