package com.jmafla.taskflow.web.mapper;

import com.jmafla.taskflow.domain.model.Task;
import com.jmafla.taskflow.web.dto.request.TaskRequest;
import com.jmafla.taskflow.web.dto.response.TaskResponse;
import org.springframework.stereotype.Component;

/** Traduce entre la entidad {@link Task} y sus DTOs. */
@Component
public class TaskMapper {

    /**
     * Convierte la entidad a su representacion publica.
     *
     * <p>Los campos {@code overdue} y {@code allowedTransitions} son derivados:
     * no se almacenan, se calculan desde el dominio en cada lectura.</p>
     */
    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getAssignee(),
                task.getDueDate(),
                task.isOverdue(),
                task.getStatus().getAllowedTransitions(),
                task.getCompletedAt(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }

    /** Construye una entidad nueva a partir de la solicitud. */
    public Task toEntity(TaskRequest request) {
        return Task.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priorityOrDefault())
                .assignee(request.assignee())
                .dueDate(request.dueDate())
                .build();
    }
}
