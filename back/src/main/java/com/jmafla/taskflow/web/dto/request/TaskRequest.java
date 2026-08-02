package com.jmafla.taskflow.web.dto.request;

import com.jmafla.taskflow.domain.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Datos de entrada para crear o actualizar una tarea.
 *
 * <p>El estado no se incluye a proposito: se cambia unicamente por el endpoint
 * dedicado {@code PATCH /tasks/{id}/status}, que valida la transicion. Permitir
 * modificarlo aqui dejaria una puerta trasera a la maquina de estados.</p>
 */
public record TaskRequest(

        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 150, message = "El titulo no puede superar 150 caracteres")
        String title,

        @Size(max = 1000, message = "La descripcion no puede superar 1000 caracteres")
        String description,

        TaskPriority priority,

        @Size(max = 80, message = "El responsable no puede superar 80 caracteres")
        String assignee,

        LocalDate dueDate
) {

    /** Prioridad efectiva: MEDIUM cuando el cliente no la especifica. */
    public TaskPriority priorityOrDefault() {
        return priority != null ? priority : TaskPriority.MEDIUM;
    }
}
