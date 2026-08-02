package com.jmafla.taskflow.web.dto.request;

import com.jmafla.taskflow.domain.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo del cambio de estado de una tarea.
 *
 * @param status estado destino solicitado
 */
public record TaskStatusRequest(

        @NotNull(message = "El estado destino es obligatorio")
        TaskStatus status
) {
}
